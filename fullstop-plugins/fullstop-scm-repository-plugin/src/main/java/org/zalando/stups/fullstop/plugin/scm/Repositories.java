package org.zalando.stups.fullstop.plugin.scm;

import org.zalando.stups.fullstop.plugin.scm.config.ScmRepositoryPluginProperties;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class Repositories {

    private static final String GITHUB_REGEX_TEMPLATE =
            "^(?:git:)?(?:git@|https?:\\/\\/)?(?<host>%s)(?::\\d+)?(?::|\\/)(?<owner>.+?)\\/(?<name>.+?)(?:\\.git)?(?:\\/.*)?$";

    private final Pattern pattern;

    public Repositories(ScmRepositoryPluginProperties properties) {
        final String hostsPattern = properties.getHosts().keySet().stream().map(Pattern::quote).collect(joining("|"));
        pattern = Pattern.compile(format(GITHUB_REGEX_TEMPLATE, hostsPattern));
    }

    Repository parse(@Nonnull String url) throws UnknownScmUrlException {
        final String lowerCaseUrl = url.toLowerCase();
        final Matcher matcher = pattern.matcher(lowerCaseUrl);
        if (matcher.matches()) {
            return new Repository(
                    matcher.group("host"),
                    matcher.group("owner"),
                    matcher.group("name"));
        } else {
            throw new UnknownScmUrlException(url);
        }
    }
}
