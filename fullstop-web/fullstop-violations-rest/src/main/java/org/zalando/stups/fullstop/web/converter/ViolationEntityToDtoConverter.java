/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    public ViolationEntityToDtoConverter(Converter<ViolationTypeEntity, ViolationType> violationTypeConverter) {
        this.violationTypeConverter = violationTypeConverter;
    }

    @Override
    public Violation convert(ViolationEntity source) {
        Violation violation = new Violation();

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

        Optional.ofNullable(source.getViolationTypeEntity())
                .map(violationTypeConverter::convert)
                .ifPresent(violation::setViolationType);

        return violation;
    }


}
