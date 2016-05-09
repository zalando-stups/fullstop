package org.zalando.stups.fullstop.violation.entity;

import com.google.common.base.Objects;
import org.joda.time.DateTime;
import org.zalando.stups.fullstop.domain.AbstractModifiableEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by gkneitschel.
 */
@Table(name = "lifecycle", schema = "fullstop_data")
@Entity
public class LifecycleEntity extends AbstractModifiableEntity {
    private DateTime eventDate;

    private String region;

    private String accountId;

    private String imageId;

    private String imageName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application", referencedColumnName = "id")
    private ApplicationEntity applicationEntity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_version", referencedColumnName = "id")
    private VersionEntity versionEntity;

    private String eventType;

    private String userdataPath;

    private DateTime instanceBootTime;

    private String instanceId;

    public DateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(final DateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public ApplicationEntity getApplicationEntity() {
        return applicationEntity;
    }

    public void setApplicationEntity(final ApplicationEntity applicationEntity) {
        this.applicationEntity = applicationEntity;
    }

    public VersionEntity getVersionEntity() {
        return versionEntity;
    }

    public void setVersionEntity(final VersionEntity versionEntity) {
        this.versionEntity = versionEntity;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    public String getUserdataPath() {
        return userdataPath;
    }

    public void setUserdataPath(final String userdataPath) {
        this.userdataPath = userdataPath;
    }

    public DateTime getInstanceBootTime() {
        return instanceBootTime;
    }

    public void setInstanceBootTime(final DateTime instanceBootTime) {
        this.instanceBootTime = instanceBootTime;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(final String instanceId) {
        this.instanceId = instanceId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(final String imageId) {
        this.imageId = imageId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(final String imageName) {
        this.imageName = imageName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final LifecycleEntity that = (LifecycleEntity) o;
        return Objects.equal(eventDate, that.eventDate) &&
                Objects.equal(region, that.region) &&
                Objects.equal(accountId, that.accountId) &&
                Objects.equal(imageId, that.imageId) &&
                Objects.equal(imageName, that.imageName) &&
                Objects.equal(applicationEntity, that.applicationEntity) &&
                Objects.equal(versionEntity, that.versionEntity) &&
                Objects.equal(eventType, that.eventType) &&
                Objects.equal(userdataPath, that.userdataPath) &&
                Objects.equal(instanceBootTime, that.instanceBootTime) &&
                Objects.equal(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eventDate, region, accountId, imageId, imageName, applicationEntity, versionEntity, eventType, userdataPath, instanceBootTime, instanceId);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("eventDate", eventDate)
                .add("accountId", accountId)
                .add("imageId", imageId)
                .add("imageName", imageName)
                .add("region", region)
                .add("applicationEntity", applicationEntity)
                .add("versionEntity", versionEntity)
                .add("eventType", eventType)
                .add("userdataPath", userdataPath)
                .add("instanceBootTime", instanceBootTime)
                .add("instanceId", instanceId)
                .toString();
    }
}