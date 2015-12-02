package org.zalando.stups.fullstop.violation.reactor;

import org.springframework.boot.actuate.metrics.CounterService;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;
import reactor.bus.Event;
import reactor.bus.EventBus;

/**
 * Uses the {@link EventBus} to transport Violations to interested Consumers.
 *
 * @author jbellmann
 */
public class EventBusViolationSink implements ViolationSink {

    private static final String VIOLATIONS_EVENTBUS_QUEUED = "violations.eventbus.queued";

    private static final String VIOLATIONS_EVENTBUS_PUT = "violations.eventbus.put";

    private static final String DEFAULT_VIOLATIONS_TOPIC = "/violations";

    private final EventBus eventBus;

    private final CounterService counterService;

    public EventBusViolationSink(final EventBus eventBus, final CounterService counterService) {
        this.eventBus = eventBus;
        this.counterService = counterService;
    }

    @Override
    public void put(final Violation violation) {
        eventBus.notify(DEFAULT_VIOLATIONS_TOPIC, Event.wrap(violation));
        counterService.increment(VIOLATIONS_EVENTBUS_QUEUED);
        counterService.increment(VIOLATIONS_EVENTBUS_PUT);
    }

}
