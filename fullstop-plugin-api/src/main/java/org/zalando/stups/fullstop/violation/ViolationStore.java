package org.zalando.stups.fullstop.violation;

/**
 * We to somehow/somewhere store the findings.
 *
 * @author  jbellmann
 */
public interface ViolationStore {

    void save(Object violation);

}
