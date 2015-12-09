package org.zalando.stups.fullstop.aws;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;

public interface ClientProvider {

    <T extends AmazonWebServiceClient> T getClient(Class<T> type, String accountId, Region region);
}
