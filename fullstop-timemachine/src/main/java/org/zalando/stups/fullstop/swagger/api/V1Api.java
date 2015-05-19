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
package org.zalando.stups.fullstop.swagger.api;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.zalando.stups.fullstop.swagger.model.Acknowledged;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.swagger.model.Violation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/v1", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/v1", description = "the v1 API")
public class V1Api {


    @ApiOperation(value = "Violations for one account", notes = "Get all violations for one account", response = Violation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of all violations for one account")})
    @RequestMapping(value = "/accountViolations/{account_id}", method = RequestMethod.GET)
    public ResponseEntity<Violation> orgZalandoStupsFullstopApiv1Violationsaccount(@ApiParam(value = "", required = true) @PathVariable("accountId") String accountId)
            throws NotFoundException {
        // do some magic!
        return new ResponseEntity<Violation>(HttpStatus.OK);
    }


    @ApiOperation(value = "Put instance log in S3", notes = "Add log for instance in S3", response = Void.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Logs saved successfully")})
    @RequestMapping(value = "/instances/logs", method = RequestMethod.PUT)
    public ResponseEntity<Void> orgZalandoStupsFullstopApiv1Instancelogs(@ApiParam(value = "", required = true) LogObj log)
            throws NotFoundException {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }


    @ApiOperation(value = "violations", notes = "Get all violations", response = Violation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of all violations")})
    @RequestMapping(value = "/violations", method = RequestMethod.GET)
    public ResponseEntity<Violation> orgZalandoStupsFullstopApiv1Violations()
            throws NotFoundException {
        // do some magic!
        return new ResponseEntity<Violation>(HttpStatus.OK);
    }


    @ApiOperation(value = "Comment and acknowledged violation", notes = "Comment and acknowledged violation", response = Void.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Violation updated successfully")})
    @RequestMapping(value = "/violations/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> orgZalandoStupsFullstopApiv1Violationsack(@ApiParam(value = "", required = true) @PathVariable("id") String id,
                                                                          @ApiParam(value = "", required = true) Acknowledged acknowledged)
            throws NotFoundException {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }


}
