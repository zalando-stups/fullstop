package com.unknown.comp;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationHandler;

/**
 * @author  jbellmann
 */
public class SimpleDemonstrationViolationHandler implements ViolationHandler {

    private final Logger log = LoggerFactory.getLogger(SimpleDemonstrationViolationHandler.class);
    private AtomicInteger counter = new AtomicInteger();

    @Override
    public void handle(final Violation violation) {
        counter.incrementAndGet();
        log.warn("HERE IT COMES FROM THE EVENT_BUS : {}", violation.toString());
    }

    public int getCount() {
        return counter.get();
    }

}
