package org.zalando.stups.fullstop.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/whitelisting-rules", produces = APPLICATION_JSON_VALUE)
@PreAuthorize("#oauth2.hasScope('uid')")
public class WhitelistController {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private RuleEntityRepository ruleEntityRepository;


    @Autowired
    public WhitelistController(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }


    @RequestMapping(value = "/", method = GET)
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public List<RuleEntity> ruleEntities() {

        return ruleEntityRepository.findAll();
    }

    @RequestMapping(value = "/", method = POST)
    @ApiResponses(value = {@ApiResponse(code = 201, message = "New rule saved successfully")})
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(CREATED)
    public RuleEntity addWhitelisting(@RequestBody RuleDTO ruleDTO) {

        RuleEntity ruleEntity = new RuleEntity();
        ruleEntity = mapRuleToRuleEntity(ruleDTO, ruleEntity);

        RuleEntity savedRuleEntity = ruleEntityRepository.save(ruleEntity);
        log.info("New Whitelisting Rule created {}", ruleEntity);
        return savedRuleEntity;
    }

    @RequestMapping(value = "/{id}", method = GET)
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public RuleEntity getWhitelisting(@PathVariable("id")
                                      final Long id) {

        return ruleEntityRepository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = PUT)
    @PreAuthorize("#oauth2.hasScope('uid')")
    @ResponseStatus(OK)
    public void updateRule(@RequestBody RuleDTO ruleDTO,
                           @PathVariable("id") final Long id) {
        RuleEntity ruleEntity = ruleEntityRepository.findOne(id);
        ruleEntity = mapRuleToRuleEntity(ruleDTO, ruleEntity);

        ruleEntityRepository.save(ruleEntity);
        log.info("Whitelisting Rule updated {}", ruleEntity);

    }


    private RuleEntity mapRuleToRuleEntity(@RequestBody RuleDTO ruleDTO, RuleEntity ruleEntity) {
        ruleEntity.setAccountId(ruleDTO.getAccountId());
        ruleEntity.setApplicationId(ruleDTO.getApplicationId());
        ruleEntity.setApplicationVersion(ruleDTO.getApplicationVersion());
        ruleEntity.setImageName(ruleDTO.getImageName());
        ruleEntity.setImageOwner(ruleDTO.getImageOwner());
        ruleEntity.setReason(ruleDTO.getReason());
        ruleEntity.setExpiryDate(ruleDTO.getExpiryDate());
        ruleEntity.setViolationTypeEntity(ruleDTO.getViolationTypeEntity());

        return ruleEntity;
    }

}
