package org.zalando.stups.fullstop.violation.service;

import org.zalando.stups.fullstop.violation.entity.Stack;

public interface ApplicationVersionService {

    Stack saveStack(String applicationId, String applicationVersion);
}
