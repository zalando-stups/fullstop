package org.zalando.stups.fullstop.jobs.annotation;

import org.springframework.scheduling.annotation.Scheduled;

import java.lang.annotation.*;

/**
 * @author jbellmann
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
public @interface TenSeconds {
}
