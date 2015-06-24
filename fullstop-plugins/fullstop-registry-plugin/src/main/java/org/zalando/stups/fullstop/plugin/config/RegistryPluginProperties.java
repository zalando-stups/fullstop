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
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author npiccolotto
 */
@ConfigurationProperties(prefix = "fullstop.plugins.registry")
public class RegistryPluginProperties {

    private List<String> defaultMandatoryApprovals = Lists.newArrayList(
            "CODE_CHANGE",
            "TEST",
            "SPECIFICATION",
            "DEPLOY");

    private List<String> mandatoryApprovals = Lists.newArrayList();

    private List<String> defaultApprovalsFromMany = Lists.newArrayList(
            "TEST",
            "CODE_CHANGE",
            "DEPLOY");

    private List<String> approvalsFromMany = Lists.newArrayList();

    public List<String> getMandatoryApprovals() {
        if (mandatoryApprovals.isEmpty()) {
            return defaultMandatoryApprovals;
        }

        return mandatoryApprovals;
    }

    public void setMandatoryApprovals(List<String> defaultApprovals) {
        this.mandatoryApprovals = defaultApprovals;
    }

    public List<String> getDefaultMandatoryApprovals() {
        return defaultMandatoryApprovals;
    }

    public void setDefaultMandatoryApprovals(List<String> defaultMandatoryApprovals) {
        this.defaultMandatoryApprovals = defaultMandatoryApprovals;
    }

    public List<String> getDefaultApprovalsFromMany() {
        return defaultApprovalsFromMany;
    }

    public void setDefaultApprovalsFromMany(List<String> defaultApprovalsFromMany) {
        this.defaultApprovalsFromMany = defaultApprovalsFromMany;
    }

    public List<String> getApprovalsFromMany() {
        if (approvalsFromMany.isEmpty()) {
            return defaultApprovalsFromMany;
        }
        return approvalsFromMany;
    }

    public void setApprovalsFromMany(List<String> approvalsFromMany) {
        this.approvalsFromMany = approvalsFromMany;
    }

}
