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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.s3.LogType;
import org.zalando.stups.fullstop.s3.S3Service;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import java.io.IOException;

import static org.joda.time.DateTimeZone.UTC;
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

    @ApiOperation(value = "Put instance log in S3", notes = "Add log for instance in S3", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Logs saved successfully") })
    @RequestMapping(value = "/instance-logs", method = RequestMethod.POST)
    @PreAuthorize("#oauth2.hasScope('uid')")
    public ResponseEntity<Void> instanceLogs(@ApiParam(value = "", required = true)
    @RequestBody final LogObj log) throws NotFoundException {
        saveLog(log);

        return new ResponseEntity<>(CREATED);
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
            log.info("Saving S3 logs with userdatapath: {}", userdataPath);

        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        if (instanceLog.getLogType() == LogType.USER_DATA) {
            LifecycleEntity lifecycleEntity = applicationLifecycleService.saveInstanceLogLifecycle(
                    instanceLog.getInstanceId(),
                    new DateTime(instanceLog.getInstanceBootTime(), UTC),
                    userdataPath,
                    instanceLog.getRegion(),
                    instanceLog.getLogData(),
                    instanceLog.getAccountId());
            log.info("Saving Lifecycle Entity: {}", lifecycleEntity);

        }
    }
}
