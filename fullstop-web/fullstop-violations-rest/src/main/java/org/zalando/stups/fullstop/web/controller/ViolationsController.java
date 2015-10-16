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

import io.swagger.annotations.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zalando.fullstop.web.api.ForbiddenException;
import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.violation.service.ViolationService;
import org.zalando.stups.fullstop.web.model.Violation;
import org.zalando.stups.fullstop.web.model.ViolationType;

import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/violations", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/violations", description = "the violations API")
public class ViolationsController {

    @Autowired
    private ViolationService violationService;

    @Autowired
    private TeamOperations teamOperations;

    @ApiOperation(
            value = "violations", notes = "Get one violation", response = Violation.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Violation")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Violation getViolation(
            @ApiParam(value = "Violation id")
            @PathVariable(value = "id")
            final Long id) throws NotFoundException {
        Violation violation = mapToDto(violationService.findOne(id));
        if (violation == null) {
            throw new NotFoundException("Violation with id: " + id + " not found!");
        }
        return violation;
    }

    @ApiOperation(
            value = "violations", notes = "Get all violations", response = Violation.class, responseContainer = "List"
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List of all violations")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @RequestMapping(method = RequestMethod.GET)
    public Page<Violation> violations(
            @ApiParam(value = "Include only violations in these accounts")
            @RequestParam(value = "accounts", required = false)
            final List<String> accounts,
            @ApiParam(value = "Include only violations that happened after this point in time")
            @RequestParam(value = "since", required = false)
            @DateTimeFormat(iso = DATE_TIME)
            final DateTime since,
            @ApiParam(value = "Include only violations after the one with this id")
            @RequestParam(value = "last-violation", required = false)
            final Long lastViolation,
            @ApiParam(value = "Include only violations where checked field equals this value")
            @RequestParam(value = "checked", required = false)
            final Boolean checked,
            @ApiParam(value = "Include only violations with a certain severity")
            @RequestParam(value = "severity", required = false)
            final Integer severity,
            @ApiParam(value = "Include only violations that are audit relevant")
            @RequestParam(value = "audit-relevant", required = false)
            final Boolean auditRelevant,
            @ApiParam(value = "Include only violations with a certain type")
            @RequestParam(value = "type", required = false)
            final String type,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = ASC) final Pageable pageable) throws NotFoundException {
        return mapBackendToFrontendViolations(
                violationService.queryViolations(
                        accounts, since, lastViolation,
                        checked, severity, auditRelevant, type, pageable));
    }

    @ApiOperation(
            value = "Resolve and explain this violation", notes = "Resolve and explain violation", response = Void.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Violation resolved successfully")})
    @RequestMapping(value = "/{id}/resolution", method = RequestMethod.POST)
    @PreAuthorize("#oauth2.hasScope('uid')")
    public Violation resolveViolations(
            @ApiParam(value = "", required = true)
            @PathVariable("id")
            final Long id,
            @ApiParam(value = "", required = true)
            @RequestBody final String comment,
            @AuthenticationPrincipal(errorOnInvalidType = true) String userId)
            throws NotFoundException, ForbiddenException {
        final ViolationEntity violation = violationService.findOne(id);

        if (violation == null) {
            throw new NotFoundException(format("Violation %s does not exist", id));
        }

        if (!hasAccessToAccount(userId, violation.getAccountId())) {
            throw new ForbiddenException(
                    format(
                            "You must have access to AWS account '%s' to resolve violation '%s'",
                            violation.getAccountId(), id));
        }

        violation.setComment(comment);
        final ViolationEntity dbViolationEntity = violationService.save(violation);
        return mapToDto(dbViolationEntity);
    }

    private static Violation mapToDto(ViolationEntity entity) {

        if (entity == null) {
            return null;
        }

        Violation violation = new Violation();

        violation.setId(entity.getId());
        violation.setVersion(entity.getVersion());

        violation.setCreated(entity.getCreated());
        violation.setCreatedBy(entity.getCreatedBy());
        violation.setLastModified(entity.getLastModified());
        violation.setLastModifiedBy(entity.getLastModifiedBy());

        violation.setAccountId(entity.getAccountId());
        violation.setEventId(entity.getEventId());

        violation.setPluginFullyQualifiedClassName(entity.getPluginFullyQualifiedClassName());

        ViolationTypeEntity violationTypeEntity = entity.getViolationTypeEntity();

        if (entity.getViolationTypeEntity() != null) {
            ViolationType violationType = new ViolationType();

            violationType.setId(violationTypeEntity.getId());
            violationType.setHelpText(violationTypeEntity.getHelpText());
            violationType.setIsAuditRelevant(violationTypeEntity.isAuditRelevant());
            violationType.setViolationSeverity(violationTypeEntity.getViolationSeverity());
            violationType.setCreated(violationTypeEntity.getCreated());
            violationType.setCreatedBy(violationTypeEntity.getCreatedBy());
            violationType.setLastModified(violationTypeEntity.getLastModified());
            violationType.setLastModifiedBy(violationTypeEntity.getLastModifiedBy());
            violationType.setVersion(violationTypeEntity.getVersion());

            violation.setViolationType(violationType);
        }

        violation.setRegion(entity.getRegion());
        violation.setInstanceId(entity.getInstanceId());
        violation.setComment(entity.getComment());
        violation.setMetaInfo(entity.getMetaInfo());
        return violation;
    }

    private boolean hasAccessToAccount(final String userId, final String targetAccountId) {
        final List<Account> teams = teamOperations.getTeamsByUser(userId);

        for (Account team : teams) {
            if (team.getId().equals(targetAccountId)) {
                return true;
            }
        }
        return false;
    }

    private Page<Violation> mapBackendToFrontendViolations(final Page<ViolationEntity> backendViolations) {
        final PageRequest currentPageRequest = new PageRequest(
                backendViolations.getNumber(),
                backendViolations.getSize(),
                backendViolations.getSort());
        return new PageImpl<>(
                backendViolations.getContent().stream().map(ViolationsController::mapToDto).collect(toList()),
                currentPageRequest,
                backendViolations.getTotalElements());
    }
}
