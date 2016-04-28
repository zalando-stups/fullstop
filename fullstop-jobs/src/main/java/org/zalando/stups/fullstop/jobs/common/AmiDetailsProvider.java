package org.zalando.stups.fullstop.jobs.common;

import com.amazonaws.regions.Region;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;

public interface AmiDetailsProvider {

    @Cacheable(cacheNames = "ami-details", cacheManager = "oneDayTTLCacheManager")
    Map<String, String> getAmiDetails(String accountId, Region region, String amiId);

}
