package org.zalando.stups.fullstop.plugin.provider;

import com.amazonaws.services.ec2.model.Image;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;

import java.util.Optional;
import java.util.function.Function;

public interface AmiProvider extends Function<EC2InstanceContext, Optional<Image>> {
}
