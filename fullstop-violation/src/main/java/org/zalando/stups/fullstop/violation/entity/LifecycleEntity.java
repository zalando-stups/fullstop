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

import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;
import org.zalando.stups.fullstop.violation.domain.AbstractModifiableEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by gkneitschel.
 */
@Table(name = "lifecycle", schema = "fullstop_data")
@Entity
public class LifecycleEntity  extends AbstractModifiableEntity {
    private DateTime startdate;

    private DateTime enddate;

    private String region;

    private Integer appHasVersionId;

    private String eventType;

    public LifecycleEntity(DateTime startdate, DateTime enddate, String region, Integer appHasVersionId,
            String eventType) {
        this.startdate = startdate;
        this.enddate = enddate;
        this.region = region;
        this.appHasVersionId = appHasVersionId;
        this.eventType = eventType;
    }

    public DateTime getStartdate() {
        return startdate;
    }

    public void setStartdate(DateTime startdate) {
        this.startdate = startdate;
    }

    public DateTime getEnddate() {
        return enddate;
    }

    public void setEnddate(DateTime enddate) {
        this.enddate = enddate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getAppHasVersionId() {
        return appHasVersionId;
    }

    public void setAppHasVersionId(Integer appHasVersionId) {
        this.appHasVersionId = appHasVersionId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("startdate", startdate)
                .add("enddate", enddate)
                .add("region", region)
                .add("appHasVersionId", appHasVersionId)
                .add("eventType", eventType)
                .toString();
    }
}