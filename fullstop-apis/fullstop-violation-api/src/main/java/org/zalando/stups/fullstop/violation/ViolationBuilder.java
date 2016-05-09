package org.zalando.stups.fullstop.violation;

/**
 * Created by gkneitschel.
 */
public class ViolationBuilder {
    private String eventId;

    private String accountId;

    private String region;

    private String instanceId;

    private Object metaInfo;

    private String type;

    private String username;

    private String pluginFullyQualifiedClassName;

    private String applicationId;

    private String applicationVersion;



    public ViolationBuilder() {
    }

    public Violation build() {
        final DefaultViolation violation = new DefaultViolation();

        violation.setEventId(eventId);
        violation.setAccountId(accountId);
        violation.setRegion(region);
        violation.setInstanceId(instanceId);
        violation.setMetaInfo(metaInfo);
        violation.setViolationType(type);
        violation.setUsername(username);
        violation.setPluginFullyQualifiedClassName(pluginFullyQualifiedClassName);
        violation.setApplicationId(applicationId);
        violation.setApplicationVersion(applicationVersion);

        return violation;
    }

    public ViolationBuilder withEventId(final String eventId) {
        this.eventId = eventId;
        return this;
    }

    public ViolationBuilder withAccountId(final String accountId) {
        this.accountId = accountId;
        return this;
    }

    public ViolationBuilder withRegion(final String region) {
        this.region = region;
        return this;
    }

    public ViolationBuilder withInstanceId(final String instanceId){
        this.instanceId = instanceId;
        return this;
    }

    public ViolationBuilder withMetaInfo(final Object metaInfo) {
        this.metaInfo = metaInfo;
        return this;
    }

    public ViolationBuilder withType(final String type) {
        this.type = type;
        return this;
    }

    public ViolationBuilder withPluginFullyQualifiedClassName(final Class pluginFullyQualifiedClassName) {
        this.pluginFullyQualifiedClassName = pluginFullyQualifiedClassName.getName();
        return this;
    }

    public ViolationBuilder withUsername(final String username) {
        this.username = username;
        return this;
    }


    public ViolationBuilder withApplicationId(final String applicationId) {
        this.applicationId = applicationId;
        return this;
    }


    public ViolationBuilder withApplicationVersion(final String applicationVersion) {
        this.applicationVersion = applicationVersion;
        return this;
    }
}
