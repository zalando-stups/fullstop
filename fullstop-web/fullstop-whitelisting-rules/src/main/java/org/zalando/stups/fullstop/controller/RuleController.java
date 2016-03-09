package org.zalando.stups.fullstop.controller;

import com.google.common.base.Preconditions;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zalando.stups.fullstop.web.api.ForbiddenException;
import org.zalando.stups.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.config.RuleControllerProperties;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;
import org.zalando.stups.fullstop.teams.TeamOperations;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/whitelisting-rules", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/whitelisting-rules", description = "Create, read, update rules for whitelisting violations")
public class RuleController {

    @Autowired
    RuleEntityService ruleEntityService;

    @Autowired
    private TeamOperations teamOperations;

    @Autowired
    private RuleControllerProperties ruleControllerProperties;


    @RequestMapping(method = GET)
    @ApiOperation(value = "Shows a list of all rules", response = RuleEntity.class, responseContainer = "List",
            authorizations = {@Authorization(value = "oauth",
                    scopes = {@AuthorizationScope(scope = "uid", description = "")})}) // TODO only valid rules?
    @ApiResponses(value = {@ApiResponse(code = 200, message = "There you go")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public List<RuleEntity> showWhitelistings(@AuthenticationPrincipal(errorOnInvalidType = true) String userId) throws ForbiddenException {

        checkPermission(userId);

        return ruleEntityService.findAll();
    }


    @RequestMapping(method = POST)
    @ApiOperation(value = "adds a new rule for whitelisting violations",
            authorizations = {@Authorization(value = "oauth",
                    scopes = {@AuthorizationScope(scope = "uid", description = "")})})
    @ApiResponses(value = {@ApiResponse(code = 201, message = "New rule saved successfully")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(CREATED)
    public RuleEntity addWhitelisting(@RequestBody RuleDTO ruleDTO, @AuthenticationPrincipal(errorOnInvalidType = true) String userId) throws ForbiddenException {

        checkPermission(userId);
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
                                      final Long id, @AuthenticationPrincipal(errorOnInvalidType = true) String userId) throws NotFoundException, ForbiddenException {
        checkPermission(userId);
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
    public RuleEntity updateWhitelisting(@RequestBody RuleDTO ruleDTO,
                                         @PathVariable("id") final Long id,
                                         @AuthenticationPrincipal(errorOnInvalidType = true) String userId)
            throws ForbiddenException, NotFoundException {

        checkPermission(userId);

        RuleEntity ruleEntity;

        try {
            ruleEntity = ruleEntityService.update(ruleDTO, id);
        } catch (NoSuchElementException e) {
            throw new NotFoundException(e.getMessage());
        }

        return ruleEntity;
    }


    private void checkPermission(String userId) throws ForbiddenException {
        Preconditions.checkNotNull(userId);
        final Set<String> teams = teamOperations.getTeamIdsByUser(userId);
        final List<String> allowedTeams = ruleControllerProperties.getAllowedTeams();

        if (allowedTeams.stream().noneMatch(teams::contains)){
            throw new ForbiddenException(format("%s has no permission! Found Teams: %s; required teams %s", userId, teams, allowedTeams));
        }
    }
}
