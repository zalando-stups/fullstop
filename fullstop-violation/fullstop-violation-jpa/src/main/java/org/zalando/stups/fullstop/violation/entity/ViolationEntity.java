package org.zalando.stups.fullstop.violation.entity;

import org.zalando.stups.fullstop.domain.AbstractModifiableEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author mrandi
 */
@Table(name = "violation", schema = "fullstop_data", uniqueConstraints = @UniqueConstraint(name = "unique_violation", columnNames = {
        "eventId", "accountId", "region", "violation_type_entity_id" }))
@Entity
public class ViolationEntity extends AbstractModifiableEntity {

    private String eventId;

    private String accountId;

    private String region;

    private String instanceId;

    private Object metaInfo;

    private String comment;

    private String pluginFullyQualifiedClassName;

    private String username;

    @ManyToOne
    private ViolationTypeEntity violationTypeEntity;

    public ViolationEntity(String eventId, String accountId, String region, String instanceId, Object metaInfo,
            String comment, String username) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.region = region;
        this.instanceId = instanceId;
        this.metaInfo = metaInfo;
        this.comment = comment;
        this.username = username;
    }

    public ViolationEntity() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
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

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPluginFullyQualifiedClassName() {
        return pluginFullyQualifiedClassName;
    }

    public void setPluginFullyQualifiedClassName(String pluginFullyQualifiedClassName) {
        this.pluginFullyQualifiedClassName = pluginFullyQualifiedClassName;
    }

    public ViolationTypeEntity getViolationTypeEntity() {
        return violationTypeEntity;
    }

    public void setViolationTypeEntity(
            ViolationTypeEntity violationTypeEntity) {
        this.violationTypeEntity = violationTypeEntity;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("eventId", eventId)
                .add("accountId", accountId)
                .add("region", region)
                .add("instanceId", instanceId)
                .add("metaInfo", metaInfo)
                .add("comment", comment)
                .add("pluginFullyQualifiedClassName", pluginFullyQualifiedClassName)
                .add("username", username)
                .add("violationTypeEntity", violationTypeEntity)
                .toString();
    }
}
