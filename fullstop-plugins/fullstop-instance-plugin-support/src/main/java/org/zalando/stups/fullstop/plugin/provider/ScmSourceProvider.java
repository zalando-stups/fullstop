package org.zalando.stups.fullstop.plugin.provider;

import org.zalando.stups.fullstop.plugin.EC2InstanceContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface ScmSourceProvider extends Function<EC2InstanceContext, Optional<Map<String, String>>> {
}
