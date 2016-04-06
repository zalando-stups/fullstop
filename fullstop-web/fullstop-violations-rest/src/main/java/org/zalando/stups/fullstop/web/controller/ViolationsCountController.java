package org.zalando.stups.fullstop.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.violation.entity.CountByAccountAndType;
import org.zalando.stups.fullstop.violation.entity.CountByAppVersionAndType;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/violation-count", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/violation-count", description = "the violations count API")
public class ViolationsCountController {

    private final ViolationRepository violationRepository;

    @Autowired
    public ViolationsCountController(ViolationRepository violationRepository) {
        this.violationRepository = violationRepository;
    }

    @RequestMapping(method = GET)
    @ApiResponses(@ApiResponse(code=200, message = "Violation count by account and type",
            response = CountByAccountAndType.class, responseContainer = "List"))
    public List<CountByAccountAndType> countByAccountAndTypes(
            @ApiParam("a list of account ids for filtering, leave blank to request all accounts")
            @RequestParam
            Optional<Set<String>> accounts,
            @ApiParam("include only violations, that have been created after this timestamp")
            @RequestParam
            @DateTimeFormat(iso = DATE_TIME)
            Optional<DateTime> from,
            @ApiParam("include only violations, that have been created before this timestamp")
            @RequestParam
            @DateTimeFormat(iso = DATE_TIME)
            Optional<DateTime> to,
            @ApiParam("count only violations that have been resolved (true), or that are still open (false)")
            @RequestParam(value = "resolved",required = false, defaultValue = "false")
            boolean resolved,
            @ApiParam("count only violations that have been whitelisted (true), or that are not whitelisted (false)")
            @RequestParam(value = "whitelisted",required = false, defaultValue = "false")
            boolean whitelisted){
        return violationRepository.countByAccountAndType(accounts.orElseGet(Collections::emptySet), from, to, resolved, whitelisted);
    }

    @RequestMapping(value = "/{account}", method = GET)
    @ApiResponses(@ApiResponse(code = 200, message = "Violation count of one account by app version and type",
            response = CountByAppVersionAndType.class, responseContainer = "List"))
    public List<CountByAppVersionAndType> countByAppVersionAndType(
            @ApiParam("an account id")
            @PathVariable
            String account,
            @ApiParam("include only violations, that have been created after this timestamp")
            @RequestParam
            @DateTimeFormat(iso = DATE_TIME)
            Optional<DateTime> from,
            @ApiParam("include only violations, that have been created before this timestamp")
            @RequestParam
            @DateTimeFormat(iso = DATE_TIME)
            Optional<DateTime> to,
            @ApiParam("count only violations that have been resolved (true), or that are still open (false)")
            @RequestParam(value = "resolved",required = false, defaultValue = "false")
            boolean resolved,
            @ApiParam("count only violations that have been whitelisted (true), or that are not whitelisted (false)")
            @RequestParam(value = "whitelisted",required = false, defaultValue = "false")
            boolean whitelisted) {
        return violationRepository.countByAppVersionAndType(account, from, to, resolved, whitelisted);
    }
}
