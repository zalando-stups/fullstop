package org.zalando.stups.fullstop.violation;

/**
 * @author  jbellmann
 */
public class NoOpViolationSink implements ViolationSink {

    @Override
    public void put(final Violation violation) {
        // do nothing
    }

}
