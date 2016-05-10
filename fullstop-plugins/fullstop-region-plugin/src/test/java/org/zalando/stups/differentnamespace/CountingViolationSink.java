package org.zalando.stups.differentnamespace;

import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jbellmann
 */
public class CountingViolationSink implements ViolationSink {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public void put(final Violation violation) {
        counter.incrementAndGet();
    }

    public int getInvocationCount() {
        return counter.get();
    }
}
