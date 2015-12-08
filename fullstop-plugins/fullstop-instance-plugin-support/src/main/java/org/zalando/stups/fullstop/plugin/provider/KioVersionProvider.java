package org.zalando.stups.fullstop.plugin.provider;

import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;

import java.util.Optional;
import java.util.function.Function;

public interface KioVersionProvider extends Function<EC2InstanceContext, Optional<Version>> {
}
