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
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.List;

public class UserTeam {

    private final String id;

    private final String name;

    private final List<InfrastructureAccount> infrastructureAccounts;

    public UserTeam(
            @JsonProperty("id")
            String id,
            @JsonProperty("name")
            String name,
            @JsonProperty("infrastructure-accounts")
            List<InfrastructureAccount> infrastructureAccounts) {
        this.id = id;
        this.name = name;
        this.infrastructureAccounts = infrastructureAccounts;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<InfrastructureAccount> getInfrastructureAccounts() {
        return infrastructureAccounts;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", id)
                          .add("name", name)
                          .add("infrastructureAccounts", infrastructureAccounts)
                          .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserTeam userTeam = (UserTeam) o;
        return Objects.equal(id, userTeam.id) &&
                Objects.equal(name, userTeam.name) &&
                Objects.equal(infrastructureAccounts, userTeam.infrastructureAccounts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, infrastructureAccounts);
    }
}
