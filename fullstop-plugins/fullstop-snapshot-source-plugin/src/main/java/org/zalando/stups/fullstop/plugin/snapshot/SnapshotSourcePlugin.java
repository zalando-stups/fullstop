package org.zalando.stups.fullstop.plugin.snapshot;

import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.ViolationType;

import java.util.function.Predicate;

import static java.util.Collections.singletonMap;
import static java.util.function.Predicate.isEqual;

public class SnapshotSourcePlugin extends AbstractEC2InstancePlugin {

    private static final String SNAPSHOT_REGEX = ".*?-SNAPSHOT$";

    private final ViolationSink violationSink;

    @Autowired
    public SnapshotSourcePlugin(final EC2InstanceContextProvider contextProvider, final ViolationSink violationSink) {
        super(contextProvider);
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES).or(isEqual(START_INSTANCES));
    }

    @Override
    protected void process(final EC2InstanceContext context) {
        context.getSource()
                .filter(string -> string.matches(SNAPSHOT_REGEX))
                .ifPresent(source -> violationSink.put(
                        context.violation()
                                .withType(ViolationType.EC2_WITH_A_SNAPSHOT_IMAGE)
                                .withPluginFullyQualifiedClassName(SnapshotSourcePlugin.class)
                                .withMetaInfo(singletonMap("deployment_artifact", source))
                                .build()));
    }
}
