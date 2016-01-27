package org.zalando.stups.fullstop.teams;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Account {

    private final String id;

    private final String name;
    private final String type;
    private final String description;
    private final String owner;
    private final boolean disabled;

    @JsonCreator
    public Account(
            @JsonProperty("id")
            String id,
            @JsonProperty("name")
            String name,
            @JsonProperty("type")
            String type,
            @JsonProperty("description")
            String description,
            @JsonProperty("owner")
            String owner,
            @JsonProperty("disabled")
            boolean disabled) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.owner = owner;
        this.disabled = disabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isDisabled(){
        return disabled;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("type", type)
                .add("description", description)
                .add("owner", owner)
                .add("disabled", disabled)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Account account = (Account) o;
        return Objects.equal(id, account.id) &&
                Objects.equal(name, account.name) &&
                Objects.equal(type, account.type) &&
                Objects.equal(description, account.description);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, type, description);
    }
}
