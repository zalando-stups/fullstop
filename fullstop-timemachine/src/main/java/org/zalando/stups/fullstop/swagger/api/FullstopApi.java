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

import com.wordnik.swagger.annotations.*;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zalando.stups.fullstop.s3.S3Service;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.swagger.model.Violation;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api", produces = { APPLICATION_JSON_VALUE })
@Api(value = "/api", description = "the api API")
@PreAuthorize("#oauth2.hasScope('uid')")
public class FullstopApi {

    private static final Logger logger = LoggerFactory.getLogger(FullstopApi.class);

    private static final Function<ViolationEntity, Violation> TO_DTO = entity -> mapToDto(entity);

    private static Violation mapToDto(ViolationEntity entity) {
        Violation violation = new Violation();

        violation.setId(entity.getId());
        violation.setVersion(entity.getVersion());

        violation.setCreated(entity.getCreated());
        violation.setCreatedBy(entity.getCreatedBy());
        violation.setLastModified(entity.getLastModified());
        violation.setLastModifiedBy(entity.getLastModifiedBy());

        violation.setAccountId(entity.getAccountId());
        violation.setEventId(entity.getEventId());
        violation.setMessage(entity.getMessage());
        violation.setRegion(entity.getRegion());
        violation.setComment(entity.getComment());
        violation.setViolationObject(entity.getViolationObject());
        return violation;
    }

    @Autowired
    private S3Service s3Writer;

    @Autowired
    private ViolationService violationService;

    @ApiOperation(value = "Put instance log in S3", notes = "Add log for instance in S3", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Logs saved successfully") })
    @RequestMapping(value = "/instance-logs", method = RequestMethod.POST)
    public ResponseEntity<Void> instanceLogs(@ApiParam(value = "", required = true)
    @RequestBody final LogObj log) throws NotFoundException {
        saveLog(log);

        return new ResponseEntity<>(CREATED);
    }

    @ApiOperation(
            value = "violations", notes = "Get all violations", response = Violation.class, responseContainer = "List"
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "List of all violations") })
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
        return mapBackendToFrontendViolations(violationService.queryViolations(accounts, since, lastViolation,
                checked, pageable));
    }

    @ApiOperation(
            value = "Resolve and explain this violation", notes = "Resolve and explain violation", response = Void.class
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Violation resolved successfully") })
    @RequestMapping(value = "/violations/{id}/resolution", method = RequestMethod.POST)
    public Violation resolveViolations(
            @ApiParam(value = "", required = true)
            @PathVariable("id")
            final Long id,
            @ApiParam(value = "", required = true)
            @RequestBody final String message) throws NotFoundException {
        ViolationEntity violation = violationService.findOne(id);

        violation.setComment(message);
        ViolationEntity dbViolationEntity = violationService.save(violation);

        return mapToDto(dbViolationEntity);
    }

    private Page<Violation> mapBackendToFrontendViolations(final Page<ViolationEntity> backendViolations) {
        final PageRequest currentPageRequest = new PageRequest(backendViolations.getNumber(),
                backendViolations.getSize(),
                backendViolations.getSort());
        return new PageImpl<>(
                backendViolations.getContent().stream().map(TO_DTO).collect(toList()),
                currentPageRequest,
                backendViolations.getTotalElements());
    }

    private void saveLog(final LogObj instanceLog) {

        if (instanceLog.getLogType() == null) {
            logger.error("You should use one of the allowed types.");
            throw new IllegalArgumentException("You should use one of the allowed types.");
        }

        try {
            s3Writer.writeToS3(instanceLog.getAccountId(), instanceLog.getRegion(), instanceLog.getInstanceBootTime(),
                    instanceLog.getLogData(), instanceLog.getLogType().toString(), instanceLog.getInstanceId());
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
