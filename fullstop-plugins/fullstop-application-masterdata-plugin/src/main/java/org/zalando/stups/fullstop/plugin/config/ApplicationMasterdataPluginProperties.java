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

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fullstop.plugins.applicationMasterdata")
public class ApplicationMasterdataPluginProperties {

    private List<String> validatorsEnabled = new ArrayList<String>();

    public List<String> getValidatorsEnabled() {
        return validatorsEnabled;
    }

    public void setValidatorsEnabled(final List<String> validatorsEnabled) {
        this.validatorsEnabled = validatorsEnabled;
    }

}
