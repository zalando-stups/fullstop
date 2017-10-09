package org.zalando.stups.fullstop.jobs.exception;

import com.google.common.collect.Maps;
import org.springframework.util.Assert;

import java.util.Map;

public interface JobExceptionHandler {

    void onException(Exception e, Map<String, String> context);
}
