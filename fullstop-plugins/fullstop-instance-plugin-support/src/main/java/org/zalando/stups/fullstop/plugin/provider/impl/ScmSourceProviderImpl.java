package org.zalando.stups.fullstop.plugin.provider.impl;

import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.ScmSourceProvider;
import org.zalando.stups.pierone.client.PieroneOperations;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ScmSourceProviderImpl implements ScmSourceProvider {

    private final Function<String, PieroneOperations> pieroneOperationsProvider;

    public ScmSourceProviderImpl(final Function<String, PieroneOperations> pieroneOperationsProvider) {
        this.pieroneOperationsProvider = pieroneOperationsProvider;
    }

    @Override
    public Optional<Map<String, String>> apply(EC2InstanceContext context) {
        return Optional.empty();
    }
}
