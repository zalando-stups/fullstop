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
package org.zalando.stups.fullstop.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.violation.entity.CountByAccountAndType;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/violation-count", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/violation-count", description = "the violations count API")
public class ViolationsCountController {

    private final ViolationRepository violationRepository;

    @Autowired
    public ViolationsCountController(ViolationRepository violationRepository) {
        this.violationRepository = violationRepository;
    }

    @RequestMapping(method = GET)
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ApiResponses(@ApiResponse(code=200, message = "Violation count by account and type",
            response = CountByAccountAndType.class, responseContainer = "List"))
    public List<CountByAccountAndType> countByAccountAndTypes(
            @ApiParam("a list of account ids for filtering, leave blank to request all accounts")
            @RequestParam
            Optional<Set<String>> accountIds,
            @ApiParam("include only violations, that have been created after this timestamp")
            @RequestParam
            Optional<DateTime> from,
            @ApiParam("include only violations, that have been created before this timestamp")
            @RequestParam
            Optional<DateTime> to,
            @ApiParam("count only violations that have been resolved (true), or that are still open (false)")
            @RequestParam
            Optional<Boolean> resolved) {
        return violationRepository.countByAccountAndType(accountIds.orElseGet(Collections::emptySet), from, to, resolved);
    }
}
