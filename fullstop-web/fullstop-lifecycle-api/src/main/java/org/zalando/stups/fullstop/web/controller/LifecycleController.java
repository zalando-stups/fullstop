package org.zalando.stups.fullstop.web.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.violation.entity.LifecycleEntity;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;
import org.zalando.stups.fullstop.web.model.LifecylceDTO;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/lifecycles", produces = APPLICATION_JSON_VALUE)
@Api(value = "/api/lifecycles", description = "Get the application lifecycle")
public class LifecycleController {

    private final ApplicationLifecycleService applicationLifecycleService;

    @Autowired
    public LifecycleController(final ApplicationLifecycleService applicationLifecycleService) {
        this.applicationLifecycleService = applicationLifecycleService;
    }

    @RequestMapping(value = "/applications/{name}/versions", method = GET)
    @ApiOperation(value = "Shows a list of all rules", response = LifecylceDTO.class, responseContainer = "List",
            authorizations = {@Authorization(value = "oauth",
                    scopes = {@AuthorizationScope(scope = "uid", description = "")})})
    @ApiResponses(@ApiResponse(code = 200, message = "the list of violations grouped by version, instance, created; Ordered by date"))
    public Page<LifecylceDTO> findByApplicationName(@PathVariable("name")
                                                    final String name,
                                                    @PageableDefault(page = 0, size = 10, sort = "created", direction = ASC) final Pageable pageable){
        final Page<LifecycleEntity> lifecycleEntities = applicationLifecycleService.findByApplicationNameAndVersion(name, null, pageable);
        return mapToDto(lifecycleEntities);

    }

    @RequestMapping(value = "/applications/{name}/versions/{version}", method = GET)
    @ApiOperation(value = "Shows a list of all rules", response = LifecylceDTO.class, responseContainer = "List",
            authorizations = {@Authorization(value = "oauth",
                    scopes = {@AuthorizationScope(scope = "uid", description = "")})})
    @ApiResponses(@ApiResponse(code = 200, message = "the list of violations grouped by version, instance, created; Ordered by date"))
    public Page<LifecylceDTO> findByApplicationName(@PathVariable("name")
                                                    final String name,
                                                    @PathVariable("version")
                                                    final String version,
                                                    @PageableDefault(page = 0, size = 10, sort = "created", direction = ASC) final Pageable pageable) {
        final Page<LifecycleEntity> lifecycleEntities = applicationLifecycleService.findByApplicationNameAndVersion(name, version, pageable);
        return mapToDto(lifecycleEntities);

    }

    private Page<LifecylceDTO> mapToDto(final Page<LifecycleEntity> lifecycleEntities) {
        final List<LifecylceDTO> lifecylceDTOList = newArrayList();

        for (final LifecycleEntity lifecycleEntity : lifecycleEntities) {
            final LifecylceDTO lifecylceDTO = new LifecylceDTO();

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
        final PageRequest currentPageRequest = new PageRequest(
                lifecycleEntities.getNumber(),
                lifecycleEntities.getSize(),
                lifecycleEntities.getSort());


        return new PageImpl<>(lifecylceDTOList, currentPageRequest, lifecycleEntities.getTotalElements());
    }
}
