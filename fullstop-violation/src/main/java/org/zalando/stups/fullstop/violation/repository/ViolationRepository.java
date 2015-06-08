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
package org.zalando.stups.fullstop.violation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

/**
 * Created by gkneitschel.
 */
@Repository
public interface ViolationRepository extends JpaRepository<ViolationEntity, Integer> {

    List<ViolationEntity> findByAccountId(String accountId);

    @Query(value = "SELECT DISTINCT v.account_id FROM fullstop_data.violation v", nativeQuery = true)
    List<String> findAccountId();
}
