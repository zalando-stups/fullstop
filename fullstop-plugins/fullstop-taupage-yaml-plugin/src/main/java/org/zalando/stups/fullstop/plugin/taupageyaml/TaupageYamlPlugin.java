package org.zalando.stups.fullstop.plugin.taupageyaml;

import static java.util.function.Predicate.isEqual;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_APPLICATION_ID_IN_USER_DATA;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_APPLICATION_VERSION_IN_USER_DATA;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_SOURCE_IN_USER_DATA;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_USER_DATA;

import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

public class TaupageYamlPlugin extends AbstractEC2InstancePlugin {

    private final ViolationSink violationSink;

    @Autowired
    public TaupageYamlPlugin(final EC2InstanceContextProvider contextProvider, final ViolationSink violationSink) {
        super(contextProvider);
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES);
    }

    @Override
    protected void process(final EC2InstanceContext context) {
        if (context.isTaupageAmi().orElse(false)) {
            if (!context.getTaupageYaml().isPresent()) {
                violationSink.put(
                        context.violation()
                                .withType(MISSING_USER_DATA)
                                .withPluginFullyQualifiedClassName(TaupageYamlPlugin.class)
                                .build());
                return;
            }

            if (!context.getApplicationId().isPresent()) {
                violationSink.put(
                        context.violation()
                                .withType(MISSING_APPLICATION_ID_IN_USER_DATA)
                                .withPluginFullyQualifiedClassName(TaupageYamlPlugin.class)
                                .build());
            }

            if (!context.getVersionId().isPresent()) {
                violationSink.put(
                        context.violation()
                                .withType(MISSING_APPLICATION_VERSION_IN_USER_DATA)
                                .withPluginFullyQualifiedClassName(TaupageYamlPlugin.class)
                                .build());
            }

            if (!context.getSource().isPresent()) {
                violationSink.put(
                        context.violation()
                                .withType(MISSING_SOURCE_IN_USER_DATA)
                                .withPluginFullyQualifiedClassName(TaupageYamlPlugin.class)
                                .build());
            }
        }
    }
}
