package org.zalando.stups.fullstop.plugin.impl;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstances;

public class EC2InstanceContextProviderImpl implements EC2InstanceContextProvider {

    final LoadingCache<CloudTrailEvent, List<EC2InstanceContext>> cache;

    public EC2InstanceContextProviderImpl(final ClientProvider clientProvider) {

        cache = newBuilder()
                .expireAfterAccess(1, MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<CloudTrailEvent, List<EC2InstanceContext>>() {
                           @Override
                           public List<EC2InstanceContext> load(@Nonnull CloudTrailEvent cloudTrailEvent) {
                               return getInstances(cloudTrailEvent)
                                       .stream()
                                       .map(i -> new EC2InstanceContextImpl(clientProvider))
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
