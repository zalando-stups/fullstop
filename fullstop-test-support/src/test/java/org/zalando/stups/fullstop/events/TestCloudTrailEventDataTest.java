/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.events;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestCloudTrailEventDataTest {

    @Test
    public void testCloudTrailEventData() throws JsonProcessingException, IOException {
        List<Map<String, Object>> records = Records.fromClasspath("/record.json");

        Map<String, Object> record = records.get(0);
        System.out.println(record.toString());

        TestCloudTrailEventData eventData = new TestCloudTrailEventData(record, "/responseElements.json");

        CloudTrailEvent event = new CloudTrailEvent(eventData, null);
        String eventName = event.getEventData().getEventName();
        System.out.println(eventName);
        System.out.println(event.getEventData().getEventSource());
        System.out.println(event.getEventData().getEventId().toString());
        System.out.println(event.getEventData().getAccountId());

        String responseElements = event.getEventData().getResponseElements();
        System.out.println(responseElements);
    }
}
