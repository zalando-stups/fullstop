package org.zalando.stups.fullstop.plugin.provider.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.ScmSourceProvider;
import org.zalando.stups.pierone.client.PieroneOperations;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

public class ScmSourceProviderImpl implements ScmSourceProvider {

    private final Logger log = getLogger(getClass());

    private final Function<String, PieroneOperations> pieroneOperationsProvider;

    private final LoadingCache<String, Optional<Map<String, String>>> cache;

    public ScmSourceProviderImpl(final Function<String, PieroneOperations> pieroneOperationsProvider) {
        this.pieroneOperationsProvider = pieroneOperationsProvider;
        this.cache = CacheBuilder.newBuilder().build(new CacheLoader<String, Optional<Map<String, String>>>() {
            @Override
            public Optional<Map<String, String>> load(@Nonnull String source) throws Exception {
                final Optional<Map<String, String>> result = scmSourceFor(source);
                if (!result.isPresent()) {
                    log.warn("Could not find scm source '{}' in Pierone", source);
                }
                return result;
            }
        });
    }

    @Override
    public Optional<Map<String, String>> apply(EC2InstanceContext context) {
        return context.getSource().flatMap(cache::getUnchecked);
    }

    private Optional<Map<String, String>> scmSourceFor(String source) {
        return Optional.of(source)
                .flatMap(PieroneImage::tryParse)
                .flatMap(this::loadSmSource);
    }

    private Optional<Map<String, String>> loadSmSource(PieroneImage image) {
        return Optional.ofNullable(image)
                .map(PieroneImage::getRepository)
                .map(pieroneOperationsProvider)
                .map(pierone -> pierone.getScmSource(image.getTeam(), image.getArtifact(), image.getTag()));
    }
}
