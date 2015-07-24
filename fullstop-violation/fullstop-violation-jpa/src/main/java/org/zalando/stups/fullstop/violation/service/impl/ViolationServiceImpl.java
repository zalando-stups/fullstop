/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.violation.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationSeverity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import java.util.List;

/**
 * Created by mrandi.
 */
@Service
public class ViolationServiceImpl implements ViolationService {

    @Autowired
    private ViolationRepository violationRepository;

    @Override
    public Page<ViolationEntity> findAll(Pageable pageable) {
        return violationRepository.findAll(pageable);
    }

    @Override
    public List<ViolationEntity> findAll() {
        return violationRepository.findAll();
    }

    @Override
    public ViolationEntity save(ViolationEntity violation) {
        return violationRepository.save(violation);
    }

    @Override
    public ViolationEntity findOne(Long id) {
        return violationRepository.findOne(id);
    }

    @Override
    public Page<ViolationEntity> queryViolations(List<String> accounts, DateTime since, Long lastViolation,
            Boolean checked, ViolationSeverity severity, Boolean auditRelevant, ViolationTypeEntity type,
            Pageable pageable) {
        return violationRepository.queryViolations(accounts, since, lastViolation, checked, severity, auditRelevant, type, pageable);
    }
}
