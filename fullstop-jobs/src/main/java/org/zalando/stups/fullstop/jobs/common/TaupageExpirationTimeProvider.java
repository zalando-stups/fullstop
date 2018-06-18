package org.zalando.stups.fullstop.jobs.common;

import java.time.ZonedDateTime;

public interface TaupageExpirationTimeProvider {

    ZonedDateTime getExpirationTime(String regionName, String imageOwner, String imageId);

}
