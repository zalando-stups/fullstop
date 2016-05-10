package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.ListenerDescription;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.jobs.common.PortsChecker;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by gkneitschel.
 */
@Component
public class PortsCheckerImpl implements PortsChecker {
    JobsProperties jobsProperties = new JobsProperties();

    @Override public List<Integer> check(final LoadBalancerDescription loadBalancerDescription) {

        return loadBalancerDescription.getListenerDescriptions()
                                      .stream()
                                      .map(ListenerDescription::getListener)
                                      .map(Listener::getLoadBalancerPort)
                                      .filter(p -> !jobsProperties.getElbAllowedPorts().contains(p))
                                      .collect(Collectors.toList());
    }

}
