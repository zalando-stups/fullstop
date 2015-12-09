package org.zalando.stups.fullstop.jobs.common;

import java.util.List;
import java.util.Optional;

public interface AwsApplications {

    /**
     * Returns true if the application, to which the given instanceIds belong to, is glagged as "publicly_accessible"
     * in Kio.
     *
     * @param accountId   the AWS account id. Must not be blank
     * @param region      the AWS region, where the instances are running
     * @param instanceIds it is assumed, that all of these instances belong to the same logical application (in Kio).
     *                    Must not be null, but contain at least one item.
     * @return an empty Optional, if none of the given instanceIds could be matched to an application in Kio.
     * Otherwise it contains the value of the field "publicly_accessible"
     */
    Optional<Boolean> isPubliclyAccessible(String accountId, String region, List<String> instanceIds);
}
