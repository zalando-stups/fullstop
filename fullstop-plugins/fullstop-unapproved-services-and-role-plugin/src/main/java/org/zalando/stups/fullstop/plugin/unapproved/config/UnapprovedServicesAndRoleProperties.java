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
package org.zalando.stups.fullstop.plugin.unapproved.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author mrandi
 */
@ConfigurationProperties(prefix = "fullstop.plugins.unapprovedServicesAndRole")
public class UnapprovedServicesAndRoleProperties {

    private static final List<String> DEFAULT_EVENT_NAMES = newArrayList(
            "CreateRole",
            "DeleteRole",
            "AttachRolePolicy",
            "UpdateAssumeRolePolicy",
            "PutRolePolicy");

    private List<String> eventNames = newArrayList();

    public List<String> getEventNames() {
        if (eventNames.isEmpty()) {
            return DEFAULT_EVENT_NAMES;
        }

        return eventNames;
    }

    public void setEventNames(final List<String> eventNames) {
        this.eventNames = eventNames;
    }

}
