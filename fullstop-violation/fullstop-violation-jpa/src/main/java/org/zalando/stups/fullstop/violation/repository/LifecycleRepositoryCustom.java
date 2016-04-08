package org.zalando.stups.fullstop.violation.repository;

import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;

import java.util.List;

public interface LifecycleRepositoryCustom {


    List<LifecycleEntity> findByApplicationNameAndVersion(String name, String version);
}
