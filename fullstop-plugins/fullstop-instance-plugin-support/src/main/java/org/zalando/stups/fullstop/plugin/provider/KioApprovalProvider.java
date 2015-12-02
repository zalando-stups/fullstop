package org.zalando.stups.fullstop.plugin.provider;

import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;

import java.util.List;
import java.util.function.Function;

public interface KioApprovalProvider extends Function<EC2InstanceContext, List<Approval>> {
}
