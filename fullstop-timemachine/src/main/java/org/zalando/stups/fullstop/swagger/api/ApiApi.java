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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zalando.stups.fullstop.swagger.model.Acknowledged;
import org.zalando.stups.fullstop.swagger.model.LogObj;
import org.zalando.stups.fullstop.swagger.model.Violation;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping(value = "/api", produces = {APPLICATION_JSON_VALUE})
@Api(value = "/api", description = "the api API")
public class ApiApi {
    @Autowired
    ViolationRepository violationRepository;


    @ApiOperation(value = "accountIds", notes = "Get all account ids", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of all account Ids") })
    @RequestMapping(value = "/accountIds",


            method = RequestMethod.GET)
    public ResponseEntity<List<String>> accountId()
            throws NotFoundException {
        List<String> accountIds = violationRepository.findAccountId();
        return new ResponseEntity<List<String>>(accountIds, HttpStatus.OK);
    }


  @ApiOperation(value = "Violations for one account", notes = "Get all violations for one account", response = Violation.class)
  @ApiResponses(value = { 
    @ApiResponse(code = 200, message = "List of all violations for one account") })
  @RequestMapping(value = "/accountViolations/{accountId}",

    
    method = RequestMethod.GET)
  public ResponseEntity<List<Violation>> accountViolations(@ApiParam(value = "",required=true ) @PathVariable("accountId") String accountId)
      throws NotFoundException {
      List<org.zalando.stups.fullstop.violation.entity.Violation> backendViolationsByAccount = violationRepository.findByAccountId(accountId);
      List<Violation> frontendViolationsByAccount = mapBackendToFrontendViolations(backendViolationsByAccount);
      return new ResponseEntity<List<Violation>>(frontendViolationsByAccount, HttpStatus.OK);
  }

  

  @ApiOperation(value = "Put instance log in S3", notes = "Add log for instance in S3", response = Void.class)
  @ApiResponses(value = { 
    @ApiResponse(code = 200, message = "Logs saved successfully") })
  @RequestMapping(value = "/instances/logs", 
    
    
    method = RequestMethod.POST)
  public ResponseEntity<Void> instanceLogs(@ApiParam(value = "" ,required=true ) LogObj log)
      throws NotFoundException {
        saveLog(log);
      return new ResponseEntity<Void>(HttpStatus.OK);
  }

    @ApiOperation(value = "violations", notes = "Get all violations", response = Violation.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "List of all violations") })
  @RequestMapping(value = "/violations",


    method = RequestMethod.GET)
  public ResponseEntity<List<Violation>> violations()
      throws NotFoundException {
      List<org.zalando.stups.fullstop.violation.entity.Violation> backendViolations = violationRepository.findAll();
      List<Violation> frontendViolations = mapBackendToFrontendViolations(backendViolations);


      return new ResponseEntity<List<Violation>>(frontendViolations, HttpStatus.OK);
  }


    @ApiOperation(value = "Comment and acknowledged violation", notes = "Comment and acknowledged violation", response = Void.class)
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Violation updated successfully") })
  @RequestMapping(value = "/violations/{id}",


    method = RequestMethod.PUT)
  public ResponseEntity<Void> acknowledgedViolations(@ApiParam(value = "",required=true ) @PathVariable("id") Integer id,
    @ApiParam(value = "" ,required=true ) Acknowledged acknowledged)
      throws NotFoundException {
        org.zalando.stups.fullstop.violation.entity.Violation violation = violationRepository.findOne(id);
        violation.setChecked(acknowledged.getChecked());
        violation.setComment(acknowledged.getMessage());
        violationRepository.save(violation);
        return new ResponseEntity<Void>(HttpStatus.OK);
  }

    private List<Violation> mapBackendToFrontendViolations(List<org.zalando.stups.fullstop.violation.entity.Violation> backendViolations) {
        List<Violation> frontendViolations = new ArrayList<>();
        for (org.zalando.stups.fullstop.violation.entity.Violation backendViolation : backendViolations) {
            Violation violation = new Violation();
            violation.setAccountId(backendViolation.getAccountId());
            violation.setEventId(backendViolation.getEventId());
            violation.setMessage(backendViolation.getMessage());
            violation.setRegion(backendViolation.getRegion());
            violation.setChecked(backendViolation.getChecked());
            violation.setComment(backendViolation.getComment());
            violation.setViolationObject(backendViolation.getViolationObject());
            frontendViolations.add(violation);
        }

        return frontendViolations;
    }


    private void saveLog(LogObj log) {
        //TODO: implement me
    }

  
}
