/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.collect.Lists;

/**
 * @author  jbellmann
 */
@ConfigurationProperties(prefix = "fullstop.plugins.region")
public class RegionPluginProperties {

    private static final List<String> DEFAULT_REGIONS = Lists.newArrayList("eu-central-1", "eu-west-1");

    private List<String> whitelistedRegions = new ArrayList<String>();

    public List<String> getWhitelistedRegions() {
        if (whitelistedRegions.isEmpty()) {
            return DEFAULT_REGIONS;
        }

        return whitelistedRegions;
    }

    public void setWhitelistedRegions(final List<String> whitelistedRegions) {
        this.whitelistedRegions = whitelistedRegions;
    }

}
