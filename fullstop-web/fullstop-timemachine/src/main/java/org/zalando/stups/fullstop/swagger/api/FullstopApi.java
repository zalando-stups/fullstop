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
package org.zalando.stups.fullstop.swagger.api;

import io.swagger.annotations.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zalando.stups.fullstop.s3.LogType;
import org.zalando.stups.fullstop.s3.S3Service;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.swagger.model.Violation;
import org.zalando.stups.fullstop.teams.InfrastructureAccount;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.teams.UserTeam;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.joda.time.DateTimeZone.UTC;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api", produces = { APPLICATION_JSON_VALUE })
@Api(value = "/api", description = "the api API")
public class FullstopApi {

    private final Logger log = LoggerFactory.getLogger(FullstopApi.class);

    @Autowired
    private S3Service s3Writer;

    @Autowired
    private ApplicationLifecycleService applicationLifecycleService;

    @Autowired
    private ViolationService violationService;

    @Autowired
    private TeamOperations teamOperations;

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
        violation.setViolationTypeEntity(entity.getViolationTypeEntity());

        violation.setRegion(entity.getRegion());
        violation.setComment(entity.getComment());
        violation.setViolationObject(entity.getMetaInfo());
        return violation;
    }

    @ApiOperation(
            value = "violations", notes = "Get one violation", response = Violation.class
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Violation") })
    @PreAuthorize("#oauth2.hasScope('uid')")
    @RequestMapping(value = "/violations/{id}", method = RequestMethod.GET)
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
    @ApiResponses(value = { @ApiResponse(code = 200, message = "List of all violations") })
    @PreAuthorize("#oauth2.hasScope('uid')")
    @RequestMapping(value = "/violations", method = RequestMethod.GET)
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
            @PageableDefault(page = 0, size = 10, sort = "id", direction = ASC) final Pageable pageable,
            @AuthenticationPrincipal(errorOnInvalidType = true) final String uid) throws NotFoundException {
        return mapBackendToFrontendViolations(
                violationService.queryViolations(
                        accounts, since, lastViolation,
                        checked, pageable));
    }

    @ApiOperation(
            value = "Resolve and explain this violation", notes = "Resolve and explain violation", response = Void.class
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Violation resolved successfully") })
    @RequestMapping(value = "/violations/{id}/resolution", method = RequestMethod.POST)
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

    @ApiOperation(value = "Put instance log in S3", notes = "Add log for instance in S3", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Logs saved successfully") })
    @RequestMapping(value = "/instance-logs", method = RequestMethod.POST)
    public ResponseEntity<Void> instanceLogs(@ApiParam(value = "", required = true)
    @RequestBody final LogObj log) throws NotFoundException {
        saveLog(log);

        return new ResponseEntity<>(CREATED);
    }

    @ExceptionHandler(ApiException.class) ResponseEntity<String> handleApiException(final ApiException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(e.getCode()));
    }

    private boolean hasAccessToAccount(final String userId, final String targetAccountId) {
        final List<UserTeam> teams = teamOperations.getTeamsByUser(userId);
        return teams.stream()
                    .flatMap(team -> team.getInfrastructureAccounts().stream())
                    .map(InfrastructureAccount::getId)
                    .anyMatch(accountId -> accountId.equals(targetAccountId));
    }

    private void saveLog(final LogObj instanceLog) {

        String userdataPath = null;
        if (instanceLog.getLogType() == null) {
            log.error("You should use one of the allowed types.");
            throw new IllegalArgumentException("You should use one of the allowed types.");
        }

        try {
            userdataPath = s3Writer.writeToS3(
                    instanceLog.getAccountId(), instanceLog.getRegion(), instanceLog.getInstanceBootTime(),
                    instanceLog.getLogData(), instanceLog.getLogType().toString(), instanceLog.getInstanceId());
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        if (instanceLog.getLogType() == LogType.USER_DATA) {
            LifecycleEntity lifecycleEntity = applicationLifecycleService.saveInstanceLogLifecycle(
                    instanceLog.getInstanceId(),
                    new DateTime(instanceLog.getInstanceBootTime(),UTC),
                    userdataPath,
                    instanceLog.getRegion(),
                    instanceLog.getLogData());
        }
    }

    private Page<Violation> mapBackendToFrontendViolations(final Page<ViolationEntity> backendViolations) {
        final PageRequest currentPageRequest = new PageRequest(
                backendViolations.getNumber(),
                backendViolations.getSize(),
                backendViolations.getSort());
        return new PageImpl<>(
                backendViolations.getContent().stream().map(FullstopApi::mapToDto).collect(toList()),
                currentPageRequest,
                backendViolations.getTotalElements());
    }

}
