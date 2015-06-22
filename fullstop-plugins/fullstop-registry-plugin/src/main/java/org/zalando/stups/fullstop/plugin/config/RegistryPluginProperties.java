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

    private static final Set<String> APPROVALS = Sets.newHashSet("CODE_CHANGE",
                                                                 "TEST",
                                                                 "SPECIFICATION",
                                                                 "DEPLOY");

    private Set<String> defaultApprovals = new HashSet<String>();

    private String codeApproval;

    private String testApproval;

    private String deployApproval;

    public Set<String> getDefaultApprovals() {
        if (defaultApprovals.isEmpty()) {
            return APPROVALS;
        }

        return defaultApprovals;
    }

    public void setDefaultApprovals(Set<String> defaultApprovals) {
        this.defaultApprovals = defaultApprovals;
    }

    public String getCodeApproval() {
        if (codeApproval == null) {
            return "CODE_CHANGE";
        }
        return codeApproval;
    }

    public void setCodeApproval(String codeApproval) {
        this.codeApproval = codeApproval;
    }

    public String getTestApproval() {
        if (testApproval == null) {
            return "TEST";
        }
        return testApproval;
    }

    public void setTestApproval(String testApproval) {
        this.testApproval = testApproval;
    }

    public String getDeployApproval() {
        if (deployApproval == null) {
            return "DEPLOY";
        }
        return deployApproval;
    }

    public void setDeployApproval(String deployApproval) {
        this.deployApproval = deployApproval;
    }

}
