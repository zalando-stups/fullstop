package org.zalando.stups.fullstop.jobs.annotation;

import org.springframework.scheduling.annotation.Scheduled;

import java.lang.annotation.*;

/**
 * @author jbellmann
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scheduled(cron = "0 23 * * * *")
public @interface EveryDayAtElevenPM {
}
