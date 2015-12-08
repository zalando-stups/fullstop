package org.zalando.stups.fullstop.violation;

/**
 * Just prints the violation to {@link System#out}.
 *
 * @author jbellmann
 */
public class SystemOutViolationHandler implements ViolationHandler {

    @Override
    public void handle(final Violation violation) {
        System.out.println(violation.toString());
    }

}
