package org.zalando.stups.fullstop.violation;

/**
 * Handles/Processes {@link Violation}s.
 *
 * @author jbellmann
 */
public interface ViolationHandler {

    void handle(Violation violation);

}
