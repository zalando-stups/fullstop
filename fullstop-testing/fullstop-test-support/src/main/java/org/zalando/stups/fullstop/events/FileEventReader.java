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
package org.zalando.stups.fullstop.events;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.interfaces.EventsProcessor;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.serializer.EventSerializer;
import com.amazonaws.services.cloudtrail.processinglibrary.utils.EventBuffer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FileEventReader {

    private final EventsProcessor eventsProcessor;

    private ObjectMapper mapper;

    public FileEventReader(final EventsProcessor eventsProcessor) {
        this.eventsProcessor = eventsProcessor;
        this.mapper = new ObjectMapper();
    }

    public void readEvents(final InputStream is) throws CallbackException {
        try (final TestCloudTrailEventSerializer serializer = new TestCloudTrailEventSerializer(mapper.getFactory().createParser(is))) {
            this.emitEvents(serializer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void emitEvents(final EventSerializer serializer) throws CallbackException, IOException {
        final EventBuffer<CloudTrailEvent> eventBuffer = new EventBuffer<>(10);
        while (serializer.hasNextEvent()) {
            CloudTrailEvent event = serializer.getNextEvent();
            eventBuffer.addEvent(event);
            if (eventBuffer.isBufferFull()) {
                this.eventsProcessor.process(eventBuffer.getEvents());
            }
        }

        // emit whatever in the buffer as last batch
        List<CloudTrailEvent> events = eventBuffer.getEvents();
        if (!events.isEmpty()) {
            this.eventsProcessor.process(events);
        }
    }
}
