package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by gkneitschel.
 */
@Table(name = "lifecycle", schema = "fullstop_data")
@Entity
public class LifecycleEntity {
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