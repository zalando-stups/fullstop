package org.zalando.stups.fullstop.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zalando.stups.fullstop.web.api.ApiException;

@ControllerAdvice
public class ApiExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(ApiException.class)
    ResponseEntity<String> handleApiException(final ApiException e) {
        log.warn("{}", e.toString());
        return new ResponseEntity<>(HttpStatus.valueOf(e.getCode()));
    }

}
