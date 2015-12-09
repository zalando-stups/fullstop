package org.zalando.stups.fullstop.violation;

/**
 * All Violations have to be written into this.
 *
 * @author jbellmann
 */
public interface ViolationSink {

    String DEFAULT_VIOLATIONS_TOPIC = "/violations";

    void put(Violation violation);

}
