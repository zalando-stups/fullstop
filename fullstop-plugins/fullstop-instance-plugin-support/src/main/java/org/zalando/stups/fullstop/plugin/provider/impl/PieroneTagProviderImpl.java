package org.zalando.stups.fullstop.plugin.provider.impl;

import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.PieroneTagProvider;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.pierone.client.TagSummary;

import java.util.Optional;

public class PieroneTagProviderImpl implements PieroneTagProvider {

    private final PieroneOperations pieroneOperations;

    public PieroneTagProviderImpl(final PieroneOperations pieroneOperations) {
        this.pieroneOperations = pieroneOperations;
    }

    @Override
    public Optional<TagSummary> apply(EC2InstanceContext context) {
        // TODO
        return Optional.empty();
    }
}
