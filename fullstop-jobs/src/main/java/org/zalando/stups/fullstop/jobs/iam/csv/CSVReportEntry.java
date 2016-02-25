package org.zalando.stups.fullstop.jobs.iam.csv;

import static com.google.common.base.MoreObjects.toStringHelper;

public class CSVReportEntry {
    private final String user;
    private final String arn;
    private final String passwordEnabled;
    private final boolean mfaActive;
    private final boolean accessKey1Active;
    private final boolean accessKey2Active;

    public CSVReportEntry(String user, String arn, String passwordEnabled, boolean mfaActive, boolean accessKey1Active, boolean accessKey2Active) {
        this.user = user;
        this.arn = arn;
        this.passwordEnabled = passwordEnabled;
        this.mfaActive = mfaActive;
        this.accessKey1Active = accessKey1Active;
        this.accessKey2Active = accessKey2Active;
    }

    public String getUser() {
        return user;
    }

    public String getArn() {
        return arn;
    }

    public String getPasswordEnabled() {
        return passwordEnabled;
    }

    public boolean isMfaActive() {
        return mfaActive;
    }

    public boolean isAccessKey1Active() {
        return accessKey1Active;
    }

    public boolean isAccessKey2Active() {
        return accessKey2Active;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("user", user)
                .add("arn", arn)
                .add("passwordEnabled", passwordEnabled)
                .add("mfaActive", mfaActive)
                .add("accessKey1Active", accessKey1Active)
                .add("accessKey2Active", accessKey2Active)
                .toString();
    }
}
