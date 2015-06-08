package org.zalando.stups.fullstop.violation;

/**
 * @author  jbellmann
 */
public class NoOpViolationHandler implements ViolationHandler {

    @Override
    public void handle(final Violation violation) {
        // do nothing
    }

}
