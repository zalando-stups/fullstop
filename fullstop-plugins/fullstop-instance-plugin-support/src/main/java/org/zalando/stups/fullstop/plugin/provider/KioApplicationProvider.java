package org.zalando.stups.fullstop.plugin.provider;

import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;

import java.util.Optional;
import java.util.function.Function;

public interface KioApplicationProvider extends Function<EC2InstanceContext, Optional<Application>> {
}
