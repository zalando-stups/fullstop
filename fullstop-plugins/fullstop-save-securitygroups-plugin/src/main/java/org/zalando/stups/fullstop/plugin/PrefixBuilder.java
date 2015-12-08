package org.zalando.stups.fullstop.plugin;

import org.joda.time.DateTime;

import java.nio.file.Paths;

/**
 * @author jbellmann
 */
class PrefixBuilder {

    /**
     * Builds the prefix that will be prepended to the 'bucketname'<br/>
     * Something like '123456789/eu-west-1/2015/06/12/'
     *
     * @param accountId
     * @param region             name of region
     * @param instanceLaunchTime
     * @return
     */
    static String build(final String accountId, final String region, final DateTime instanceLaunchTime) {
        return Paths.get(
                accountId, region, instanceLaunchTime.toString("YYYY"), instanceLaunchTime.toString("MM"),
                instanceLaunchTime.toString("dd")).toString() + "/";
    }

}
