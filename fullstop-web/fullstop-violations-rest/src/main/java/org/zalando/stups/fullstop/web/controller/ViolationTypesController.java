package org.zalando.stups.fullstop.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.violation.repository.ViolationTypeRepository;
import org.zalando.stups.fullstop.web.model.ViolationType;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/violation-types", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/violation-types", description = "Manage violation types")
public class ViolationTypesController {

    private final ViolationTypeRepository violationTypeRepository;

    private final Converter<ViolationTypeEntity, ViolationType> entityToDto;

    @Autowired
    public ViolationTypesController(ViolationTypeRepository violationTypeRepository, Converter<ViolationTypeEntity, ViolationType> entityToDto) {
        Assert.notNull(violationTypeRepository, "violationTypeRepository must not be null");
        Assert.notNull(entityToDto, "entityToDto converter must not be null");

        this.violationTypeRepository = violationTypeRepository;
        this.entityToDto = entityToDto;
    }

    @RequestMapping(method = GET)
    @ApiResponses(@ApiResponse(code = 200, message = "The list of all available violation types",
            response = ViolationType.class, responseContainer = "List"))
    @PreAuthorize("#oauth2.hasScope('uid')")
    public List<ViolationType> getAll() {
        return violationTypeRepository.findAll()
                .stream()
                .map(entityToDto::convert)
                .collect(toList());
    }
}
