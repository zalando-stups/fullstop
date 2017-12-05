package org.zalando.stups.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.lambda.LambdaPlugin;
import org.zalando.stups.fullstop.plugin.lambda.config.LambdaPluginProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

public class LambdaPluginTest {


    private ViolationSink mockViolationSink;
    private LambdaPlugin lambdaPlugin;
    private LambdaPluginProperties lambdaPluginProperties;

    @Before
    public void setUp() throws Exception {

        mockViolationSink = mock(ViolationSink.class);

        lambdaPluginProperties = new LambdaPluginProperties();
        lambdaPluginProperties.setS3Buckets(asList("zalando-lambda-repository-eu-central-1", "zalando-lambda-repository-eu-west-1"));
        lambdaPlugin = new LambdaPlugin(mockViolationSink, lambdaPluginProperties);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockViolationSink);
    }


    @Test
    public void testProcessLambdaEvent() throws Exception {
        final CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/record.json");
        lambdaPlugin.processEvent(cloudTrailEvent);

        verify(mockViolationSink, never()).put(any(Violation.class));
    }

    @Test
    public void testProcessLambdaEventWrongS3Bucket() throws Exception {
        final CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/record-wrong-s3bucket.json");
        lambdaPlugin.processEvent(cloudTrailEvent);

        verify(mockViolationSink, only()).put(any(Violation.class));
    }


}
