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
package org.zalando.stups.fullstop.violation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.Date;
import java.util.List;

/**
 * Created by mrandi.
 */
public interface ViolationService {

    Page<ViolationEntity> findAll(Pageable pageable);

    List<ViolationEntity> findAll();

    void save(ViolationEntity violation);

    ViolationEntity findOne(Long id);

    Page<ViolationEntity> queryViolations(List<String> accounts, Date since, Long lastViolation, Boolean checked, Pageable pageable);
}
