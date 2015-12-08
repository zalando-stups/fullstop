package org.zalando.stups.fullstop.jobs.iam.csv;

import com.google.common.base.MoreObjects;

public class User {
    private final String name;
    private final boolean passwordEnabled;

    public User(String name, boolean passwordEnabled) {
        this.name = name;
        this.passwordEnabled = passwordEnabled;
    }

    public String getName() {
        return name;
    }

    public boolean isPasswordEnabled() {
        return passwordEnabled;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("passwordEnabled", passwordEnabled)
                .toString();
    }
}
