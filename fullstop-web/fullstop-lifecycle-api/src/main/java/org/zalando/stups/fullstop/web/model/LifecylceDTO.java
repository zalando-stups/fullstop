package org.zalando.stups.fullstop.web.model;

import org.joda.time.DateTime;

public class LifecylceDTO {
    private String application;
    private String version;
    private DateTime eventDate;
    private String imageID;
    private String imageName;
    private String region;
    private DateTime instanceBootTime;
    private String eventType;
    private String instanceId;
    private DateTime created;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(DateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public DateTime getInstanceBootTime() {
        return instanceBootTime;
    }

    public void setInstanceBootTime(DateTime instanceBootTime) {
        this.instanceBootTime = instanceBootTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }
}
