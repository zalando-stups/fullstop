/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.snapshot.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.events.Records;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnapshotSourcePluginTest {

	private UserDataProvider provider;
	private ViolationSink sink;
	private CloudTrailEvent event;
	private SnapshotSourcePlugin plugin;

    protected CloudTrailEvent buildEvent(String type) {
        List<Map<String, Object>> records = Records.fromClasspath("/record-" + type + ".json");

        return TestCloudTrailEventData.createCloudTrailEventFromMap(records.get(0));
    }
    
    @Before
    public void setUp() {
        provider = Mockito.mock(UserDataProvider.class);
        sink = Mockito.mock(ViolationSink.class);
        plugin = new SnapshotSourcePlugin(provider, sink);
    }
    
    @After
    public void tearDown() {
    	verifyNoMoreInteractions(sink, provider, plugin);
    }
    
    @Test
    public void shouldNotSupportTerminateEvent() {
    	event = buildEvent("termination");
    	assertThat(plugin.supports(event)).isFalse();
    }
    
    @Test
    public void shouldSupportRunEvent() {
    	event = buildEvent("run");
    	assertThat(plugin.supports(event)).isTrue();
    }

    @Test
    public void shouldComplainWithoutSource() {
    	event = buildEvent("run");
    	when(provider.getUserData(any(), any()))
    		.thenReturn(new HashMap<String, String>());
    	plugin.processEvent(event);
    	
    	verify(sink).put(any(Violation.class));
    }
    
    @Test
    public void shouldComplainWithSnapshotSource() {
    	event = buildEvent("run");
    	Map<String, String> userData = new HashMap<String, String>();
    	userData.put("source", "docker://registry.zalando.com/stups/yourturn:1.0-SNAPSHOT");
    	plugin.processEvent(event);
    	
    	verify(sink).put(any(Violation.class));
    }
    
    @Test
    public void shouldNotComplainWithoutSnapshotSource() {
    	event = buildEvent("run");
    	Map<String, String> userData = new HashMap<String, String>();
    	userData.put("source", "docker://registry.zalando.com/stups/yourturn:1.0");
    	
    	verify(sink, never()).put(any(Violation.class));
    }
}
