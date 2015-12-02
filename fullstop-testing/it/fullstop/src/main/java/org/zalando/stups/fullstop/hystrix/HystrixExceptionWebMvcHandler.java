package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ControllerAdvice
public class HystrixExceptionWebMvcHandler {

    private final Logger log = getLogger(getClass());

    @ResponseStatus(value = SERVICE_UNAVAILABLE, reason = "Dependency unavailable")
    @ExceptionHandler(HystrixRuntimeException.class) void handleRuntimeException(final HystrixRuntimeException e) {
        log.warn("Request failed due to unavailable dependency.", e);
    }
}
