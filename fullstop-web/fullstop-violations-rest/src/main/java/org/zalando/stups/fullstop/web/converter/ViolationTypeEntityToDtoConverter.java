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

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.web.model.ViolationType;

@Component
public class ViolationTypeEntityToDtoConverter implements Converter<ViolationTypeEntity, ViolationType> {

    @Override
    public ViolationType convert(ViolationTypeEntity source) {
        final ViolationType target = new ViolationType();
        target.setId(source.getId());
        target.setHelpText(source.getHelpText());
        target.setIsAuditRelevant(source.isAuditRelevant());
        target.setViolationSeverity(source.getViolationSeverity());
        target.setCreated(source.getCreated());
        target.setCreatedBy(source.getCreatedBy());
        target.setLastModified(source.getLastModified());
        target.setLastModifiedBy(source.getLastModifiedBy());
        target.setVersion(source.getVersion());
        return target;
    }
}
