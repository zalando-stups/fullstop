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
     * @param   accountId
     * @param   region              name of region
     * @param   instanceLaunchTime
     *
     * @return
     */
    static String build(final String accountId, final String region, final DateTime instanceLaunchTime) {
        return Paths.get(
                accountId, region, instanceLaunchTime.toString("YYYY"), instanceLaunchTime.toString("MM"),
                instanceLaunchTime.toString("dd")).toString() + "/";
    }

}
