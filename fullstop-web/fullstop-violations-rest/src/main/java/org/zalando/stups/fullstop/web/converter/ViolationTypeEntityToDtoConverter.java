package org.zalando.stups.fullstop.web.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.web.model.ViolationType;

@Component
public class ViolationTypeEntityToDtoConverter implements Converter<ViolationTypeEntity, ViolationType> {

    @Override
    public ViolationType convert(final ViolationTypeEntity source) {
        final ViolationType target = new ViolationType();
        target.setId(source.getId());
        target.setHelpText(source.getHelpText());
        target.setIsAuditRelevant(source.isAuditRelevant());
        target.setViolationSeverity(source.getViolationSeverity());
        target.setPriority(source.getPriority());
        target.setCreated(source.getCreated());
        target.setCreatedBy(source.getCreatedBy());
        target.setLastModified(source.getLastModified());
        target.setLastModifiedBy(source.getLastModifiedBy());
        target.setVersion(source.getVersion());
        return target;
    }
}
