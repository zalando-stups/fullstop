/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation.store.slf4j;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.StringUtils;

import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationStore;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import com.google.common.collect.Lists;

/**
 * Simple implementation to use the logging-framework of choice to collection violations.
 *
 * @author  jbellmann
 */
public class Slf4jViolationStore implements ViolationStore {

    private static final String DEFAULT_LOGGER_NAME = "fullstop.violations.store";

    @Autowired
    private ViolationRepository violationRepository;

    public static final String VIOLATION = "VIOLATION: ";

    private final List<Logger> loggers = Lists.newArrayList();

    /**
     * Uses the default logger 'fullstop.violations.store'.
     */
    public Slf4jViolationStore() {
        loggers.add(LoggerFactory.getLogger(DEFAULT_LOGGER_NAME));
    }

    public Slf4jViolationStore(final List<String> loggernames) {
        for (String loggername : loggernames) {
            if (StringUtils.hasText(loggername)) {
                loggers.add(LoggerFactory.getLogger(loggername));
            }
        }
    }

    @Override
    public void save(final Violation violation) {
        StringBuilder sb = new StringBuilder();
        sb.append(VIOLATION);
        sb.append(violation.toString());
        for (Logger logger : loggers) {
            logger.info(sb.toString());
        }

        violationRepository.save(violation);
    }

}
