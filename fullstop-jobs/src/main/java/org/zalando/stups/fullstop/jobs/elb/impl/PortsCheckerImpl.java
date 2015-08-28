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
package org.zalando.stups.fullstop.jobs.elb.impl;

import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.elb.PortsChecker;
import org.zalando.stups.fullstop.jobs.utils.Predicates;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by gkneitschel.
 */
@Component
public class PortsCheckerImpl implements PortsChecker {
    JobsProperties jobsProperties = new JobsProperties();

    @Override public List<Integer> check(LoadBalancerDescription loadBalancerDescription) {

        return loadBalancerDescription.getListenerDescriptions()
                                      .stream()
                                      .map(ListenerDescription::getListener)
                                      .map(Listener::getLoadBalancerPort)
                                      .filter(
                                              Predicates.allowedPorts(
                                                      jobsProperties.getAllowedPorts()))
                                      .collect(Collectors.toList());
    }

}
