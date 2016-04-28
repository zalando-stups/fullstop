package org.zalando.stups.fullstop.violation.service;

import org.zalando.stups.fullstop.violation.entity.Stack;

import java.util.Optional;

public interface ApplicationVersionService {

    Optional<Stack> saveStack(String applicationId, String applicationVersion);
}
