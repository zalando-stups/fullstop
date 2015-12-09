package org.zalando.stups.fullstop.violation;

/**
 * @author jbellmann
 */
public class SystemOutViolationSink implements ViolationSink {

    @Override
    public void put(final Violation violation) {
        System.out.println(violation.toString());
    }

}
