package org.zalando.stups.fullstop.controller;

import io.swagger.annotations.*;
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

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/whitelisting-rules", produces = APPLICATION_JSON_VALUE)
@Api(value = "/whitelisting-rules", description = "Create, read, update rules for whitelisting violations")
@PreAuthorize("#oauth2.hasScope('uid')")
public class RuleController {

    @Autowired
    RuleEntityService ruleEntityService;

    private final Logger log = LoggerFactory.getLogger(getClass());


    @RequestMapping(value = "/", method = GET)
    @ApiOperation(value = "Shows a list of all rules", response = RuleEntity.class, responseContainer = "List",
            authorizations = {@Authorization(value = "oauth",
                    scopes = {@AuthorizationScope(scope = "uid", description = "")})}) // TODO only valid rules?
    @ApiResponses(value = {@ApiResponse(code = 200, message = "There you go")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public List<RuleEntity> showWhitelistings() {

        return ruleEntityService.findAll();
    }

    @RequestMapping(value = "/", method = POST)
    @ApiOperation(value = "adds a new rule for whitelisting violations",
            authorizations = {@Authorization(value = "oauth",
                    scopes = {@AuthorizationScope(scope = "uid", description = "")})})
    @ApiResponses(value = {@ApiResponse(code = 201, message = "New rule saved successfully")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(CREATED)
    public RuleEntity addWhitelisting(@RequestBody RuleDTO ruleDTO) {

        return ruleEntityService.save(ruleDTO);
    }

    @RequestMapping(value = "/{id}", method = GET)
    @ApiOperation(value = "adds a new rule for whitelisting violations", response = RuleEntity.class,
            authorizations = {@Authorization(value = "oauth",
                    scopes = {@AuthorizationScope(scope = "uid", description = "")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "There you go")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public RuleEntity getWhitelisting(@PathVariable("id")
                                      final Long id) throws NotFoundException {

        RuleEntity ruleEntity = ruleEntityService.findById(id);
        if (ruleEntity == null) {
            throw new NotFoundException(format("No such Rule! Id: %s", id));
        }
        return ruleEntity;
    }

    @RequestMapping(value = "/{id}", method = PUT)
    @ApiOperation(value = "updates a rule by invalidating it and creating a new rule",
            authorizations = {@Authorization(value = "oauth", scopes = {@AuthorizationScope(scope = "uid", description = "")})})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Updated")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public void updateWhitelisting(@RequestBody RuleDTO ruleDTO,
                                   @PathVariable("id") final Long id) throws NotFoundException {
        RuleEntity updatedRuleEntity = ruleEntityService.update(ruleDTO, id);
        if (updatedRuleEntity == null) {
            throw new NotFoundException(format("No such Rule! Id: %s", id));
        }

        log.info("Rule {} succesfully updated", id);

    }
}
