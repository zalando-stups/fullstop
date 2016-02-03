package org.zalando.stups.fullstop.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/whitelisting-rule", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("#oauth2.hasScope('uid')")
public class WhitelistController {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private RuleEntity ruleEntity;

    @Autowired
    public WhitelistController(RuleEntity ruleEntity) {
        this.ruleEntity = ruleEntity;
    }

    @RequestMapping(value = "/add", method = POST)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Rule saved successfully") })
    @PreAuthorize("#oauth2.hasScope('uid')")
    public ResponseEntity<Void> addWhitelisting(@RequestBody RuleEntity ruleEntity){

        log.info("New Whitelisting Rule created {}", ruleEntity);

        return new ResponseEntity<>(CREATED);
    }

    @RequestMapping(value = "/get", method = GET)
    @PreAuthorize("#oauth2.hasScope('uid')")
    public RuleEntity getWhitelisting(){

        return ruleEntity;
    }

}
