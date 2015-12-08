package org.zalando.stups.fullstop.web.test;

/**
 * @author ahartmann
 */
public interface TestObjectBuilder<ENTITY_TYPE> {

    ENTITY_TYPE build();
}
