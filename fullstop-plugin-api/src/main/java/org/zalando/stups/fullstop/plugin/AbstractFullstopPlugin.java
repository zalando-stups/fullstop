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
package org.zalando.stups.fullstop.plugin;

import org.springframework.plugin.metadata.PluginMetadata;

import org.zalando.stups.fullstop.events.CloudtrailEventSupport;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

/**
 * Base that can be used to implement a {@link FullstopPlugin}.
 *
 * @author  jbellmann
 */
public abstract class AbstractFullstopPlugin implements FullstopPlugin {

    @Override
    public PluginMetadata getMetadata() {

        return new DefaultMetadataProvider(getClass().getName()).getMetadata();
    }

    protected String getCloudTrailEventId(final CloudTrailEvent event) {
        return CloudtrailEventSupport.getEventId(event);
    }

    protected String getCloudTrailEventAccountId(final CloudTrailEvent event) {
        return CloudtrailEventSupport.getAccountId(event);
    }

    protected String getCloudTrailEventRegion(final CloudTrailEvent event) {
        return CloudtrailEventSupport.getAccountId(event);
    }

}
