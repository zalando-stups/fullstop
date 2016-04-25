package org.zalando.stups.fullstop.plugin.keypair;

import com.amazonaws.services.ec2.model.Image;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.zalando.stups.fullstop.violation.ViolationType.EC2_WITH_KEYPAIR;

public class KeyPairPlugin extends AbstractEC2InstancePlugin {

    private final ViolationSink violationSink;

    public KeyPairPlugin(EC2InstanceContextProvider contextProvider, final ViolationSink violationSink) {
        super(contextProvider);
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        // A key pair can only be assigned to an EC instance at launch time.
        // Once the key pair is set it cannot be changed.
        return isEqual(RUN_INSTANCES);
    }

    @Override
    protected void process(EC2InstanceContext context) {
        getKeyName(context).ifPresent(
                k -> violationSink.put(
                        context.violation()
                                .withType(EC2_WITH_KEYPAIR)
                                .withPluginFullyQualifiedClassName(KeyPairPlugin.class)
                                .withMetaInfo(ImmutableMap.of(
                                        "key_name", k,
                                        "ami_owner_id", context.getAmi().map(Image::getOwnerId).orElse(""),
                                        "ami_id", context.getAmiId().orElse(""),
                                        "ami_name", context.getAmi().map(Image::getName).orElse("")))
                                .build()));
    }

    private Optional<String> getKeyName(EC2InstanceContext context) {
        try {
            return Optional.ofNullable(trimToNull(JsonPath.read(context.getInstanceJson(), "$.keyName")));
        } catch (JsonPathException ignored) {
            return Optional.empty();
        }
    }


}
