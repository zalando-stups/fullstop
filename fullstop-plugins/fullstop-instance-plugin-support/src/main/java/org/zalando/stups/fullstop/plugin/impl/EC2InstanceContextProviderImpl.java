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
package org.zalando.stups.fullstop.plugin.impl;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.provider.AmiIdProvider;
import org.zalando.stups.fullstop.plugin.provider.AmiProvider;
import org.zalando.stups.fullstop.plugin.provider.TaupageYamlProvider;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstances;

public class EC2InstanceContextProviderImpl implements EC2InstanceContextProvider {

    final LoadingCache<CloudTrailEvent, List<EC2InstanceContext>> cache;

    public EC2InstanceContextProviderImpl(
            final ClientProvider clientProvider,
            final AmiIdProvider amiIdProvider,
            final AmiProvider amiProvider,
            final TaupageYamlProvider taupageYamlProvider,
            final String taupageNamePrefix,
            final String taupageOwner) {
        cache = newBuilder()
                .expireAfterAccess(1, MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<CloudTrailEvent, List<EC2InstanceContext>>() {
                           @Override
                           public List<EC2InstanceContext> load(@Nonnull final CloudTrailEvent cloudTrailEvent) {
                               return getInstances(cloudTrailEvent)
                                       .stream()
                                       .map(instanceJson -> new EC2InstanceContextImpl(
                                               cloudTrailEvent,
                                               instanceJson,
                                               clientProvider,
                                               amiIdProvider,
                                               amiProvider,
                                               taupageYamlProvider,
                                               taupageNamePrefix,
                                               taupageOwner))
                                       .collect(toList());
                           }
                       }
                );
    }

    @Override
    public List<EC2InstanceContext> instancesIn(CloudTrailEvent event) {
        return cache.getUnchecked(event);
    }
}
