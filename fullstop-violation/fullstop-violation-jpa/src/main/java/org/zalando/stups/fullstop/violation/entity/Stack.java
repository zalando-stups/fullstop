package org.zalando.stups.fullstop.violation.entity;

public class Stack {

    private final ApplicationEntity applicationEntity;

    private final VersionEntity versionEntity;

    public Stack(final ApplicationEntity applicationEntity, final VersionEntity versionEntity) {

        this.applicationEntity = applicationEntity;
        this.versionEntity = versionEntity;
    }

    public ApplicationEntity getApplicationEntity() {
        return applicationEntity;
    }

    public VersionEntity getVersionEntity() {
        return versionEntity;
    }
}
