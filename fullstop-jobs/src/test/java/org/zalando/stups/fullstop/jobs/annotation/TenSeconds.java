package org.zalando.stups.fullstop.jobs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author  jbellmann
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scheduled(initialDelay = 10 * 1000, fixedDelay = 10 * 1000)
public @interface TenSeconds { }
