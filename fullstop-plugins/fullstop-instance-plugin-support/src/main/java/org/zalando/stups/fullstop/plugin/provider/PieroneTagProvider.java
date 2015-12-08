package org.zalando.stups.fullstop.plugin.provider;

import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.pierone.client.TagSummary;

import java.util.Optional;
import java.util.function.Function;

public interface PieroneTagProvider extends Function<EC2InstanceContext, Optional<TagSummary>> {
}
