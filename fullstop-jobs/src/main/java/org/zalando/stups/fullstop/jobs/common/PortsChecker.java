package org.zalando.stups.fullstop.jobs.common;

import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;

import java.util.List;

/**
 * Created by gkneitschel.
 */
public interface PortsChecker {
    List<Integer> check (LoadBalancerDescription loadBalancerDescription);
}
