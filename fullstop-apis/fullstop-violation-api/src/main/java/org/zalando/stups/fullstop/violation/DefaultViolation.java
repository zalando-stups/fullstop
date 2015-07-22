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
package org.zalando.stups.fullstop.violation;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author jbellmann
 */
class DefaultViolation implements Violation {

    private String eventId;

    private String accountId;

    private String region;

    private Object metaInfo;

    private String comment;

    private Boolean checked;

    private String pluginFullQualifiedClassName;

    private String violationType;

    DefaultViolation(final String eventId, final String accountId, final String region, final String message,
            final Object metaInfo, final String comment, final Boolean checked) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.region = region;
        this.metaInfo = metaInfo;
        this.comment = comment;
        this.checked = checked;
    }

    DefaultViolation() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public Object getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(final Object metaInfo) {
        this.metaInfo = metaInfo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(final Boolean checked) {
        this.checked = checked;
    }

    @Override
    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    @Override
    public String getPluginFullQualifiedClassName() {
        return pluginFullQualifiedClassName;
    }

    public void setPluginFullQualifiedClassName(String pluginFullQualifiedClassName) {
        this.pluginFullQualifiedClassName = pluginFullQualifiedClassName;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("eventId", eventId)
                .add("accountId", accountId)
                .add("region", region)
                .add("metaInfo", metaInfo)
                .add("comment", comment)
                .add("checked", checked)
                .add("pluginFullQualifiedClassName", pluginFullQualifiedClassName)
                .add("violationType", violationType)
                .toString();
    }
}
