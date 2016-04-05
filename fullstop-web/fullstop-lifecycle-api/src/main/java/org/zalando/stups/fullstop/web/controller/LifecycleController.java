package org.zalando.stups.fullstop.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;
import org.zalando.stups.fullstop.web.model.LifecylceDTO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/lifecycle", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/lifecycle", description = "Get the application lifecycle")
public class LifecycleController {

    private ApplicationLifecycleService applicationLifecycleService;

    @Autowired
    public LifecycleController(ApplicationLifecycleService applicationLifecycleService) {
        this.applicationLifecycleService = applicationLifecycleService;
    }


    @RequestMapping(value = "/app/{name}", method = GET)
    @ApiResponses(@ApiResponse(code = 200, message = "the list of violations grouped by version, instance, created; Ordered by date",
    response = LifecylceDTO.class, responseContainer = "List"))
    public  List<LifecylceDTO> findByApplicationName(@PathVariable("name")
                                                 final String name) {
        List<LifecycleEntity> lifecycleEntities = applicationLifecycleService.findByApplicationName(name);
        return mapToDto(lifecycleEntities);

    }

    private List<LifecylceDTO> mapToDto(List<LifecycleEntity> lifecycleEntities) {
        List<LifecylceDTO> lifecylceDTOList = newArrayList();

        for (LifecycleEntity lifecycleEntity : lifecycleEntities) {
            LifecylceDTO lifecylceDTO = new LifecylceDTO();

            lifecylceDTO.setApplication(lifecycleEntity.getApplicationEntity().getName());
            lifecylceDTO.setCreated(lifecycleEntity.getCreated());
            lifecylceDTO.setEventDate(lifecycleEntity.getEventDate());
            lifecylceDTO.setEventType(lifecycleEntity.getEventType());
            lifecylceDTO.setImageID(lifecycleEntity.getImageId());
            lifecylceDTO.setImageName(lifecycleEntity.getImageName());
            lifecylceDTO.setInstanceBootTime(lifecycleEntity.getInstanceBootTime());
            lifecylceDTO.setInstanceId(lifecycleEntity.getInstanceId());
            lifecylceDTO.setRegion(lifecycleEntity.getRegion());
            lifecylceDTO.setVersion(lifecycleEntity.getVersionEntity().getName());

            lifecylceDTOList.add(lifecylceDTO);
        }

        return lifecylceDTOList;
    }
}
