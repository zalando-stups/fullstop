package org.zalando.stups.fullstop.violation.store.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.stups.fullstop.violation.ViolationStore;

/**
 * Simple implementation to use the logging-framework of choice to collection violations.
 *
 * @author  jbellmann
 */
public class Slf4jViolationStore implements ViolationStore {

    private static final String LOGGER_NAME = "fullstop.violations.store";

    private final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    @Override
    public void save(final Object violation) {
        logger.info(violation.toString());
    }

}
