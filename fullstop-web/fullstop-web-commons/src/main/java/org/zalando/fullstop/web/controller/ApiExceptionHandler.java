package org.zalando.fullstop.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zalando.fullstop.web.api.ApiException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<String> handleApiException(final ApiException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(e.getCode()));
    }

}
