package org.zalando.stups.fullstop.aws;

import com.amazonaws.regions.Region;

/**
 * Just for testing at the moment.
 *
 * @author  jbellmann
 */
public interface CachingClientProvider {

    <T> T getClient(Class<T> type, String accountId, Region region);
}
