package org.zalando.stups.fullstop.plugin.count;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jbellmann
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
            }
            else {
                final Meter created = metricRegistry.meter(name);
                meters.put(name, created);
                return created;
            }
        }
    }

    public void markEvent(final String event) {
        getOrCreateMeter(eventMeters, METER + event).mark();
    }
}
