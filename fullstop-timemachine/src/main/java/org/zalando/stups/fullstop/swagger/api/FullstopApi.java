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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zalando.stups.fullstop.s3.S3Writer;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.swagger.model.Violation;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(
    value = "/api",
    produces = { APPLICATION_JSON_VALUE }
)
@Api(
    value = "/api",
    description = "the api API"
)
@PreAuthorize("#oauth2.hasScope('uid')")
public class FullstopApi {

    private static final Logger logger = LoggerFactory.getLogger(
            FullstopApi.class);

    @Autowired private S3Writer s3Writer;
    @Autowired private ViolationService violationService;

    @ApiOperation(
        value = "Put instance log in S3",
        notes = "Add log for instance in S3",
        response = Void.class
    )
    @ApiResponses(
        value = {
                @ApiResponse(
                    code = 201,
                    message = "Logs saved successfully"
                )
            }
    )
    @RequestMapping(
        value = "/instance-logs",
        method = RequestMethod.POST
    )
    public ResponseEntity<Void> instanceLogs(
        @ApiParam(
            value = "",
            required = true
        )
        @RequestBody LogObj log) throws NotFoundException {
        saveLog(log);

        return new ResponseEntity<>(CREATED);
    }

    @ApiOperation(value = "violations", notes = "Get all violations", response = Violation.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of all violations") })
    @RequestMapping(value = "/violations",
            method = RequestMethod.GET)
    public List<Violation> violations(
        @ApiParam(value = "Include only violations in these accounts")
        @RequestParam(
            value = "accounts",
            required = false
        )
        List<String> accounts,
        @ApiParam(
            value =
                "Include only violations that happened after this point in time"
        )
        @RequestParam(
            value = "since",
            required = false
        )
        Date since,
        @ApiParam(value = "Include only violations after the one with this id")
        @RequestParam(
            value = "last-violation",
            required = false
        )
        Long lastViolation,
        @ApiParam(
            value =
                "Include only violations where checked field equals this value"
        )
        @RequestParam(
            value = "checked",
            required = false
        )
        Boolean checked,
        @PageableDefault(
            page = 0,
            size = 10,
            sort = "id",
            direction = ASC
        ) final Pageable pageable,
        @AuthenticationPrincipal(errorOnInvalidType = true) final String uid)
        throws NotFoundException {
        logger.info("this is my username: {}", uid);

        Page<ViolationEntity> backendViolations = violationService.queryViolations(accounts,since,lastViolation,
                checked,pageable);

        return mapBackendToFrontendViolations(backendViolations.getContent());
    }

    @ApiOperation(
        value = "Resolve and explain this violation",
        notes = "Resolve and explain violation",
        response = Void.class
    )
    @ApiResponses(
        value = {
                @ApiResponse(
                    code = 200,
                    message = "Violation resolved successfully"
                )
            }
    )
    @RequestMapping(
        value = "/violations/{id}/resolution",
        method = RequestMethod.POST
    )
    public ResponseEntity<Void> resolveViolations(
        @ApiParam(
            value = "",
            required = true
        )
        @PathVariable("id")
        Long id,
        @ApiParam(
            value = "",
            required = true
        ) @RequestBody String message) throws NotFoundException {
        ViolationEntity violation = violationService.findOne(id);

        violation.setComment(message);
        violationService.save(violation);

        return new ResponseEntity<>(OK);
    }

    private List<Violation> mapBackendToFrontendViolations(
        final List<ViolationEntity> backendViolations) {
        List<Violation> frontendViolations = newArrayList();

        for (ViolationEntity entity : backendViolations) {
            Violation violation = new Violation();

            violation.setId(entity.getId());
            violation.setVersion(entity.getVersion());

            violation.setCreated(entity.getCreated().toDate());
            violation.setCreatedBy(entity.getCreatedBy());
            violation.setLastModified(entity.getLastModified().toDate());
            violation.setLastModifiedBy(entity.getLastModifiedBy());

            violation.setAccountId(entity.getAccountId());
            violation.setEventId(entity.getEventId());
            violation.setMessage(entity.getMessage());
            violation.setRegion(entity.getRegion());
            violation.setComment(entity.getComment());
            violation.setViolationObject(entity.getViolationObject());
            frontendViolations.add(violation);
        }

        return frontendViolations;
    }

    private void saveLog(final LogObj instanceLog) {

        if (instanceLog.getLogType() == null) {
            logger.error("You should use one of the allowed types.");
            throw new IllegalArgumentException("You should use one of the allowed types.");
        }

        try {
            s3Writer.writeToS3(instanceLog.getAccountId(),
                instanceLog.getRegion(), instanceLog.getInstanceBootTime(),
                instanceLog.getLogData(), instanceLog.getLogType().toString(),
                instanceLog.getInstanceId());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
