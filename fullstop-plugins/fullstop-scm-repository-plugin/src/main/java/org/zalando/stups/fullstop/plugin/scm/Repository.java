package org.zalando.stups.fullstop.plugin.scm;

import java.util.Objects;

public class Repository {

    private final Provider provider;
    private final String host;
    private final String owner;
    private final String name;

    Repository(Provider provider, String host, String owner, String name) {
        this.provider = provider;
        this.host = host;
        this.owner = owner;
        this.name = name;
    }

    String getHost() {
        return host;
    }

    String getOwner() {
        return owner;
    }

    String getName() {
        return name;
    }

    public Provider getProvider() {
        return provider;
    }

    @Override
    public String toString() {
        return String.format(provider.getNormalizedUrlFormat(), host, owner, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repository that = (Repository) o;
        return Objects.equals(getHost(), that.getHost()) &&
                Objects.equals(getOwner(), that.getOwner()) &&
                Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getOwner(), getName());
    }
}
