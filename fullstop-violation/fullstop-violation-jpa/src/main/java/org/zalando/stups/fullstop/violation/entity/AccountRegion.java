package org.zalando.stups.fullstop.violation.entity;

import java.util.Objects;

public class AccountRegion {

    private final String account;
    private final String region;

    public AccountRegion(String account, String region) {
        this.account = account;
        this.region = region;
    }

    public String getAccount() {
        return account;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return "AccountRegion{" +
                "account='" + account + '\'' +
                ", region='" + region + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountRegion that = (AccountRegion) o;
        return Objects.equals(getAccount(), that.getAccount()) &&
                Objects.equals(getRegion(), that.getRegion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount(), getRegion());
    }
}
