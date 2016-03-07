package org.zalando.stups.fullstop.web.controller;

import io.swagger.annotations.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zalando.fullstop.web.api.ForbiddenException;
import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.service.ViolationService;
import org.zalando.stups.fullstop.web.model.Violation;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api/violations", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/violations", description = "the violations API")
public class ViolationsController {

    @Autowired
    private ViolationService violationService;

    @Autowired
    private TeamOperations teamOperations;

    @Autowired
    private Converter<ViolationEntity, Violation> entityToDto;

    @ApiOperation(
            value = "violations", notes = "Get one violation", response = Violation.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Violation")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @RequestMapping(value = "/{id}", method = GET)
    public Violation getViolation(
            @ApiParam(value = "Violation id")
            @PathVariable(value = "id")
            final Long id) throws NotFoundException {
        return Optional.ofNullable(violationService.findOne(id))
                .map(entityToDto::convert)
                .orElseThrow(() -> new NotFoundException("Violation with id: " + id + " not found!"));
    }

    @ApiOperation(
            value = "violations", notes = "Get all violations", response = Violation.class, responseContainer = "List"
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List of all violations")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @RequestMapping(method = GET)
    public Page<Violation> violations(
            @ApiParam(value = "Include only violations in these accounts")
            @RequestParam(value = "accounts", required = false)
            final List<String> accounts,
            @ApiParam(value = "Include only violations that happened after this point in time")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DATE_TIME)
            DateTime from,
            @ApiParam(value = "Include only violations that happened up to this point in time")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DATE_TIME)
            DateTime to,
            @ApiParam(value = "Include only violations after the one with this id")
            @RequestParam(value = "last-violation", required = false)
            final Long lastViolation,
            @ApiParam(value = "Include only violations where checked field equals this value (i.e. resolved violations)")
            @RequestParam(value = "checked", required = false)
            final Boolean checked,
            @ApiParam(value = "Include only violations with a certain severity")
            @RequestParam(value = "severity", required = false)
            final Integer severity,
            @ApiParam(value = "Include only violations that are audit relevant")
            @RequestParam(value = "audit-relevant", required = false)
            final Boolean auditRelevant,
            @ApiParam(value = "Include only violations with a certain type")
            @RequestParam(value = "type", required = false)
            final String type,
            @ApiParam(value = "show also whitelisted vioaltions")
            @RequestParam(value = "whitelisted")
            final Optional<Boolean> whitelisted,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = ASC) final Pageable pageable) throws NotFoundException {
        if (from == null) {
            from = DateTime.now().minusWeeks(1);
        }
        if (to == null) {
            to = DateTime.now();
        }
        return mapBackendToFrontendViolations(
                violationService.queryViolations(
                        accounts, from, to, lastViolation,
                        checked, severity, auditRelevant, type, whitelisted, pageable));
    }

    @ApiOperation(
            value = "Resolve and explain this violation", notes = "Resolve and explain violation", response = Void.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Violation resolved successfully")})
    @RequestMapping(value = "/{id}/resolution", method = POST)
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
        return entityToDto.convert(dbViolationEntity);
    }

    private boolean hasAccessToAccount(final String userId, final String targetAccountId) {
        final List<Account> teams = teamOperations.getTeamsByUser(userId);

        for (Account team : teams) {
            if (team.getId().equals(targetAccountId)) {
                return true;
            }
        }
        return false;
    }

    private Page<Violation> mapBackendToFrontendViolations(final Page<ViolationEntity> backendViolations) {
        final PageRequest currentPageRequest = new PageRequest(
                backendViolations.getNumber(),
                backendViolations.getSize(),
                backendViolations.getSort());
        return new PageImpl<>(
                backendViolations.getContent().stream().map(entityToDto::convert).collect(toList()),
                currentPageRequest,
                backendViolations.getTotalElements());
    }
}
