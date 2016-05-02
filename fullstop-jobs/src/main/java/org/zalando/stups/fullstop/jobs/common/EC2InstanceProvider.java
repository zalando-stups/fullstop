package org.zalando.stups.fullstop.jobs.common;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.model.Instance;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

public interface EC2InstanceProvider {

    @Cacheable(cacheNames = "ec2-instance", cacheManager = "twoHoursTTLCacheManager")
    Optional<Instance> getById(String accountId, Region region, String instanceId);

}
