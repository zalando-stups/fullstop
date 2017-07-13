package org.zalando.stups.fullstop.jobs.exception;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class JobExceptionHandlerImpl implements JobExceptionHandler {

    private final Logger log = getLogger(getClass());

    @Override
    public void onException(Exception e, Map<String, String> context) {
        if (e instanceof AmazonServiceException) {
            final AmazonServiceException a = (AmazonServiceException) e;
            if (a.getErrorCode().equals("RequestLimitExceeded")) {
                logWarn("RequestLimitExceeded", context);
            } else if (a instanceof AWSSecurityTokenServiceException) {
                logWarn(a.toString(), context);
            } else {
                logError(a, context);
            }
        } else {
            logError(e, context);
        }
    }

    private void logWarn(String message, Map<String, String> context) {
        log.warn("{}; Context: {}", message, context);
    }

    private void logError(Exception e, Map<String, String> context) {
        log.error(format("Message: %s; Context: %s", e.getMessage(), context), e);
    }
}
