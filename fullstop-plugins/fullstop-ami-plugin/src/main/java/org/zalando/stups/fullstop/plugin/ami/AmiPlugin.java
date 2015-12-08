package org.zalando.stups.fullstop.plugin.ami;

import com.amazonaws.services.ec2.model.Image;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_AMI;

public class AmiPlugin extends AbstractEC2InstancePlugin {

    private final ViolationSink violationSink;

    @Autowired
    public AmiPlugin(final EC2InstanceContextProvider contextProvider,
                     final ViolationSink violationSink) {
        super(contextProvider);
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES);
    }

    @Override
    protected void process(EC2InstanceContext context) {

        if (!context.isTaupageAmi().orElse(false)) {
            violationSink.put(
                    context.violation()
                            .withType(WRONG_AMI)
                            .withPluginFullyQualifiedClassName(AmiPlugin.class)
                            .withMetaInfo(ImmutableMap.of(
                                    "ami_id", context.getAmiId().orElse(""),
                                    "ami_name", context.getAmi().map(Image::getName).orElse("")))
                            .build());
        }

    }
}
