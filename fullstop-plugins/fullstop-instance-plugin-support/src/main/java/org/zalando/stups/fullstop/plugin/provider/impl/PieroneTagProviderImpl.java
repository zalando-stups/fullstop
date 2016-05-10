package org.zalando.stups.fullstop.plugin.provider.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.PieroneTagProvider;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.pierone.client.TagSummary;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class PieroneTagProviderImpl implements PieroneTagProvider {

    private final Logger log = getLogger(getClass());

    private final Function<String, PieroneOperations> pieroneOperationsProvider;

    private final LoadingCache<String, Optional<TagSummary>> cache;

    public PieroneTagProviderImpl(final Function<String, PieroneOperations> pieroneOperationsProvider) {
        this.pieroneOperationsProvider = pieroneOperationsProvider;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(5, MINUTES)
                .build(new CacheLoader<String, Optional<TagSummary>>() {
            @Override
            public Optional<TagSummary> load(@Nonnull final String source) throws Exception {
                final Optional<TagSummary> result = tagForSource(source);
                if (!result.isPresent()) {
                    log.warn("Could not find tag '{}' in Pierone", source);
                }
                return result;
            }
        });
    }

    @Override
    public Optional<TagSummary> apply(final EC2InstanceContext context) {
        return context.getSource().flatMap(cache::getUnchecked);
    }

    private Optional<TagSummary> tagForSource(@Nonnull final String source) {
        return Optional.of(source)
                .flatMap(PieroneImage::tryParse)
                .flatMap(this::loadTag);
    }

    private Optional<TagSummary> loadTag(final PieroneImage image) {
        return Optional.ofNullable(pieroneOperationsProvider.apply(image.getRepository()))
                .map(client -> client.listTags(image.getTeam(), image.getArtifact()))
                .map(result -> result.get(image.getTag()));
    }
}
