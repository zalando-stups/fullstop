package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.Before;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;

import java.util.List;
import java.util.Map;

/**
 * Created by gkneitschel.
 */
public class LifecyclePluginTest {

    private LifecyclePlugin plugin;
    private UserDataProvider provider;
    private LifecycleRepository lifecycleRepository;


    protected CloudTrailEvent buildEvent(String type) {
        List<Map<String, Object>> records = Records.fromClasspath("/record-" + type + ".json");

        return TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
    }

    @Before
    public void setUp() throws Exception {
        provider = Mockito.mock(UserDataProvider.class);
        lifecycleRepository = Mockito.mock(LifecycleRepository.class);
        plugin = new LifecyclePlugin(lifecycleRepository, provider);
    }

    public void testSupports() throws Exception {

    }

    public void testProcessEvent() throws Exception {

    }
}