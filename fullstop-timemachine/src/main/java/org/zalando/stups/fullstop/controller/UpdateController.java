/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups.fullstop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.violation.entity.Violation;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by gkneitschel.
 */
@RestController
public class UpdateController {

    @Autowired
    private ViolationRepository violationRepository;

    @RequestMapping(value = { "/violations/{id}"}, method = POST)
    public void update(@PathVariable Integer id,@RequestBody final Violation violation){
        Violation dbViolation = violationRepository.findOne(id);
        dbViolation.setChecked(violation.getChecked());
        dbViolation.setComment(violation.getComment());
        violationRepository.save(dbViolation);
    }
}
