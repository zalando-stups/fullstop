/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.teams;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

public class Account {

    private final String id;

    private final String name;
    private final String type;
    private final String description;


    public Account(
            @JsonProperty("id")
            String id,
            @JsonProperty("name")
            String name,
            @JsonProperty("type")
            String type,
            @JsonProperty("description")
            String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
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

    @Override public String toString() {
        return toStringHelper(this)
                      .add("id", id)
                      .add("name", name)
                      .add("type", type)
                      .add("description", description)
                      .toString();
    }

    @Override public boolean equals(Object o) {
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

    @Override public int hashCode() {
        return Objects.hashCode(id, name, type, description);
    }
}
