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
package org.zalando.stups.fullstop.plugin.count;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import com.google.common.collect.Maps;

/**
 * @author  jbellmann
 */
@Component
public class CountEventsMetric {

    private static final String METER = "meter.events.";

    private final MetricRegistry metricRegistry;

    private final Map<String, Meter> eventMeters = new HashMap<String, Meter>();

    @Autowired
    public CountEventsMetric(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public Map<String, Meter> getEventMeters() {
        return Maps.newHashMap(eventMeters);
    }

    protected Meter getOrCreateMeter(final Map<String, Meter> meters, final String name) {
        Meter m = meters.get(name);
        if (m != null) {
            return m;
        }

        synchronized (this) {
            m = meters.get(name);
            if (m != null) {
                return m;
            } else {
                Meter created = metricRegistry.meter(name);
                meters.put(name, created);
                return created;
            }
        }
    }

    public void markEvent(final String event) {
        getOrCreateMeter(eventMeters, METER + event).mark();
    }
}
