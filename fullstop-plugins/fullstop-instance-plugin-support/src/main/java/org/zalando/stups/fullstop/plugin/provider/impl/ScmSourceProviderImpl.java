package org.zalando.stups.fullstop.plugin.provider.impl;

import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.ScmSourceProvider;
import org.zalando.stups.pierone.client.PieroneOperations;

import java.util.Map;
import java.util.Optional;

public class ScmSourceProviderImpl implements ScmSourceProvider {

    private final PieroneOperations pieroneOperations;

    public ScmSourceProviderImpl(final PieroneOperations pieroneOperations) {
        this.pieroneOperations = pieroneOperations;
    }

    @Override
    public Optional<Map<String, String>> apply(EC2InstanceContext context) {
        return Optional.empty();
    }
}
