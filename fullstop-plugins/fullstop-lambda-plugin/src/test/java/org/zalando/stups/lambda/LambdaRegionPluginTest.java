package org.zalando.stups.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;
import org.zalando.stups.fullstop.plugin.lambda.LambdaRegionPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;
import static org.zalando.stups.fullstop.violation.ViolationMatchers.hasType;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_REGION;

public class LambdaRegionPluginTest {

    private LambdaRegionPlugin lambdaRegionPlugin;
    private ViolationSink mockViolationSink;

    @Before
    public void setUp() throws Exception {
        RegionPluginProperties props = new RegionPluginProperties();
        props.setWhitelistedRegions(asList("eu-west-1", "eu-central-1"));

        mockViolationSink = mock(ViolationSink.class);

        lambdaRegionPlugin = new LambdaRegionPlugin(mockViolationSink, props);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockViolationSink);
    }

    @Test
    public void testSupportCreateFunctionEvent() {
        final CloudTrailEvent event = createCloudTrailEvent("/record-create-correct-s3bucket.json");
        assertThat(lambdaRegionPlugin.supports(event)).isTrue();
    }

    @Test
    public void testSupportUpdateFunctionEvent() {
        final CloudTrailEvent event = createCloudTrailEvent("/record-update-correct-s3bucket.json");
        assertThat(lambdaRegionPlugin.supports(event)).isFalse();
    }

    @Test
    public void testPluginGeneratesViolationOnWrongRegion() {
        final CloudTrailEvent event = createCloudTrailEvent("/record-create-wrong-s3bucket.json");
        lambdaRegionPlugin.processEvent(event);
        verify(mockViolationSink).put(argThat(hasType(WRONG_REGION)));
    }

    @Test
    public void testPluginSkipsViolationOnCorrectRegion() {
        final CloudTrailEvent event = createCloudTrailEvent("/record-create-correct-s3bucket.json");
        lambdaRegionPlugin.processEvent(event);
    }
}
