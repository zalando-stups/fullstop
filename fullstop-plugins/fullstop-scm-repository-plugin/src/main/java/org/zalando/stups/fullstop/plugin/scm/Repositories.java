package org.zalando.stups.fullstop.plugin.scm;

import org.zalando.stups.fullstop.plugin.scm.config.ScmRepositoryPluginProperties;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.zalando.stups.fullstop.plugin.scm.Provider.GITHUB;
import static org.zalando.stups.fullstop.plugin.scm.Provider.STASH;

public class Repositories {

    private final ScmRepositoryPluginProperties properties;

    public Repositories(ScmRepositoryPluginProperties properties) {
        this.properties = properties;
    }

    Repository parse(String url) throws UnknownScmUrlException {
        for (Provider provider : asList(GITHUB, STASH)) {
            final Repository repository = tryParse(url, provider);
            if (repository != null) {
                return repository;
            }
        }

        throw new UnknownScmUrlException(url);
    }

    private Repository tryParse(String url, Provider provider) {
        final Set<String> hosts = Optional.ofNullable(provider)
                .map(properties.getHosts()::get)
                .map(Map::keySet)
                .orElseThrow(() -> new IllegalStateException("no config provided for scm repository provider " + provider));
        final String hostsPattern = hosts.stream().map(Pattern::quote).collect(joining("|"));
        final Pattern pattern = Pattern.compile(format(provider.getUrlRegexTemplate(), hostsPattern));
        final Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return new Repository(
                    provider,
                    matcher.group("host"),
                    matcher.group("owner"),
                    matcher.group("name"));
        } else {
            return null;
        }
    }
}
