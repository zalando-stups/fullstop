package org.zalando.stups.fullstop.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/lifecycle", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/lifecycle", description = "Get the application lifecycle")
public class LifecycleController {

    LifecycleRepository lifecycleRepository;

    @Autowired
    public LifecycleController(LifecycleRepository lifecycleRepository) {
        this.lifecycleRepository = lifecycleRepository;
    }


    @RequestMapping(value = "/app/{name}", method = GET)
    @ApiResponses(@ApiResponse(code = 200, message = "the list of violations grouped by version, instance; Ordered by date",
    response = LifecycleEntity.class, responseContainer = "List"))
    public  List<LifecycleEntity> findSorted(@PathVariable("name")
                                                 final String name) {
        return lifecycleRepository.findByApplicationName(name); }
}
