package org.zalando.stups.fullstop.violation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;

import java.util.List;

public interface LifecycleRepositoryCustom {


    Page<LifecycleEntity> findByApplicationNameAndVersion(String name, String version, Pageable pageable);
}
