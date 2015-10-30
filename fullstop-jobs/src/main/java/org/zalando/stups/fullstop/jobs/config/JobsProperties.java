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
package org.zalando.stups.fullstop.jobs.config;

import com.google.common.collect.Sets;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by gkneitschel.
 */
@ConfigurationProperties(prefix = "fullstop.jobs")
@Component
public class JobsProperties {
    private List<String> whitelistedRegions;
    private Set<Integer> allowedPorts = Sets.newHashSet(80, 443);
    private Set<Integer> elbAllowedPorts = Sets.newHashSet(80, 443);
    private Set<Integer> ec2AllowedPorts = Sets.newHashSet(80, 443, 22);
    private int accessKeysExpireAfterDays = 30;

    public List<String> getWhitelistedRegions() {
        return whitelistedRegions;
    }

    public Set<Integer> getAllowedPorts(){
        return allowedPorts;
    }

    public void setWhitelistedRegions(List<String> whitelistedRegions) {
        this.whitelistedRegions = whitelistedRegions;
    }

    public void setAllowedPorts(Set<Integer> allowedPorts) {
        this.allowedPorts = allowedPorts;
    }

    public Set<Integer> getElbAllowedPorts() {
        return elbAllowedPorts;
    }

    public void setElbAllowedPorts(Set<Integer> elbAllowedPorts) {
        this.elbAllowedPorts = elbAllowedPorts;
    }

    public Set<Integer> getEc2AllowedPorts() {
        return ec2AllowedPorts;
    }

    public void setEc2AllowedPorts(Set<Integer> ec2AllowedPorts) {
        this.ec2AllowedPorts = ec2AllowedPorts;
    }

    public int getAccessKeysExpireAfterDays() {
        return accessKeysExpireAfterDays;
    }

    public void setAccessKeysExpireAfterDays(int accessKeysExpireAfterDays) {
        this.accessKeysExpireAfterDays = accessKeysExpireAfterDays;
    }
}
