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
package org.zalando.stups.fullstop.plugin.provider.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.KioApplicationProvider;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Optional.*;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class KioApplicationProviderImpl implements KioApplicationProvider {

    private final Logger log = getLogger(getClass());

    private final KioOperations kioOperations;

    public KioApplicationProviderImpl(KioOperations kioOperations) {
        this.kioOperations = kioOperations;
    }

    private final LoadingCache<EC2InstanceContext, Optional<Application>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<Application>>() {
                @Override
                public Optional<Application> load(@Nonnull EC2InstanceContext context) throws Exception {
                    final Optional<Application> kioApplication = getKioApplication(context);
                    if (!kioApplication.isPresent()) {
                        log.warn("Could not find the application {} in KIO.", context);
                    }
                    return kioApplication;
                }
            });

    private Optional<Application> getKioApplication(@Nonnull EC2InstanceContext context) {

        Optional<String> applicationId = context.getApplicationId();

        if (applicationId.isPresent()) {
            return ofNullable(kioOperations.getApplicationById(applicationId.get()));
        }

        return Optional.empty();
    }

    @Override
    public Optional<Application> apply(EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }

}
