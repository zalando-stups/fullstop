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
package org.zalando.stups.fullstop.violation.entity;

import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by mrandi.
 */
@Table(name = "violation_type", schema = "fullstop_data")
@Entity
public class ViolationTypeEntity extends AbstractModifiableEntity {

    private String helpText;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ViolationSeverityEntity violationSeverityEntity;

    private boolean isAuditRelevant;

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public ViolationSeverityEntity getViolationSeverityEntity() {
        return violationSeverityEntity;
    }

    public void setViolationSeverityEntity(
            ViolationSeverityEntity violationSeverityEntity) {
        this.violationSeverityEntity = violationSeverityEntity;
    }

    public boolean isAuditRelevant() {
        return isAuditRelevant;
    }

    public void setIsAuditRelevant(boolean isAuditRelevant) {
        this.isAuditRelevant = isAuditRelevant;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("helpText", helpText)
                .add("violationSeverityEntity", violationSeverityEntity)
                .add("isAuditRelevant", isAuditRelevant)
                .toString();
    }
}
