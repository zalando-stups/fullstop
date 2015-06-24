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
package org.zalando.stups.fullstop.plugin.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author npiccolotto
 */
@ConfigurationProperties(prefix = "fullstop.plugins.registry")
public class RegistryPluginProperties {

    private Set<String> defaultMandatoryApprovals = Sets.newHashSet("CODE_CHANGE",
                                                                    "TEST",
                                                                    "SPECIFICATION",
                                                                    "DEPLOY");

    private Set<String> mandatoryApprovals = Sets.newHashSet();

    private Set<String> defaultApprovalsFromMany = Sets.newHashSet("TEST",
                                                                   "CODE_CHANGE",
                                                                   "DEPLOY");

    private Set<String> approvalsFromMany = Sets.newHashSet();

    public Set<String> getMandatoryApprovals() {
        if (mandatoryApprovals.isEmpty()) {
            return defaultMandatoryApprovals;
        }

        return mandatoryApprovals;
    }

    public void setMandatoryApprovals(Set<String> defaultApprovals) {
        this.mandatoryApprovals = defaultApprovals;
    }

    public Set<String> getDefaultMandatoryApprovals() {
        return defaultMandatoryApprovals;
    }

    public void setDefaultMandatoryApprovals(Set<String> defaultMandatoryApprovals) {
        this.defaultMandatoryApprovals = defaultMandatoryApprovals;
    }

    public Set<String> getDefaultApprovalsFromMany() {
        return defaultApprovalsFromMany;
    }

    public void setDefaultApprovalsFromMany(Set<String> defaultApprovalsFromMany) {
        this.defaultApprovalsFromMany = defaultApprovalsFromMany;
    }

    public Set<String> getApprovalsFromMany() {
        if (approvalsFromMany.isEmpty()) {
            return defaultApprovalsFromMany;
        }
        return approvalsFromMany;
    }

    public void setApprovalsFromMany(Set<String> approvalsFromMany) {
        this.approvalsFromMany = approvalsFromMany;
    }

}
