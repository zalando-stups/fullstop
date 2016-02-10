package org.zalando.stups.fullstop.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/whitelisting-rules", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("#oauth2.hasScope('uid')")
public class RuleController {

    @Autowired
    RuleEntityService ruleEntityService;

    private final Logger log = LoggerFactory.getLogger(getClass());


    //TODO Swagger for all (?)
    @RequestMapping(value = "/", method = GET)
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public List<RuleEntity> showWhitelistings() {

        return ruleEntityService.findAll();
    }

    @RequestMapping(value = "/", method = POST)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "New rule saved successfully")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(CREATED)
    public RuleEntity addWhitelisting(@RequestBody RuleDTO ruleDTO) {

        return ruleEntityService.save(ruleDTO);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public RuleEntity getWhitelisting(@PathVariable("id")
                                      final Long id) {

        return ruleEntityService.findById(id);
    }

    @RequestMapping(value = "/{id}", method = PUT)
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public void updateWhitelisting(@RequestBody RuleDTO ruleDTO,
                                   @PathVariable("id") final Long id) throws NotFoundException {
        RuleEntity updatedRuleEntity = ruleEntityService.update(ruleDTO, id);
        if (updatedRuleEntity == null) {
            throw new NotFoundException(String.format("No such Rule! {}", id));
        }

        log.info("Rule {} succesfully updated", id);

    }
}
