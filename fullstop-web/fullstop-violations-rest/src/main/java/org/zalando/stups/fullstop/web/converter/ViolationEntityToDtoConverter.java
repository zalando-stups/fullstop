package org.zalando.stups.fullstop.web.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.web.model.Violation;
import org.zalando.stups.fullstop.web.model.ViolationType;

import java.util.Optional;

@Component
public class ViolationEntityToDtoConverter implements Converter<ViolationEntity, Violation> {

    private final Converter<ViolationTypeEntity, ViolationType> violationTypeConverter;

    @Autowired
    public ViolationEntityToDtoConverter(final Converter<ViolationTypeEntity, ViolationType> violationTypeConverter) {
        this.violationTypeConverter = violationTypeConverter;
    }

    @Override
    public Violation convert(final ViolationEntity source) {
        final Violation violation = new Violation();

        violation.setId(source.getId());
        violation.setVersion(source.getVersion());

        violation.setCreated(source.getCreated());
        violation.setCreatedBy(source.getCreatedBy());
        violation.setLastModified(source.getLastModified());
        violation.setLastModifiedBy(source.getLastModifiedBy());

        violation.setAccountId(source.getAccountId());
        violation.setEventId(source.getEventId());

        violation.setPluginFullyQualifiedClassName(source.getPluginFullyQualifiedClassName());
        violation.setRegion(source.getRegion());
        violation.setInstanceId(source.getInstanceId());
        violation.setComment(source.getComment());
        violation.setMetaInfo(source.getMetaInfo());

        violation.setUsername(source.getUsername());

        if (source.getApplication() != null) {
            violation.setApplicationId(source.getApplication().getName());
        }

        if (source.getApplicationVersion() != null) {
            violation.setApplicationVersionId(source.getApplicationVersion().getName());
        }


        if (source.getRuleEntity() != null) {
            violation.setRuleID(source.getRuleEntity().getId());
        }

        Optional.ofNullable(source.getViolationTypeEntity())
                .map(violationTypeConverter::convert)
                .ifPresent(violation::setViolationType);

        return violation;
    }


}
