package org.zalando.stups.fullstop.plugin.provider.impl;

import com.google.common.base.MoreObjects;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PieroneImage {

    private static final Pattern DOCKER_SOURCE_PATTERN = Pattern.compile("^(.*)/(.+)/(.+):(.+)$");

    private final String repository;
    private final String team;
    private final String artifact;
    private final String tag;

    public PieroneImage(final String repository, final String team, final String artifact, final String tag) {
        this.repository = repository;
        this.team = team;
        this.artifact = artifact;
        this.tag = tag;
    }

    public static Optional<PieroneImage> tryParse(final String source) {
        return Optional.ofNullable(source)
                .map(DOCKER_SOURCE_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> new PieroneImage(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)));
    }

    public String getTag() {
        return tag;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getTeam() {
        return team;
    }

    public String getRepository() {
        return repository;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("repository", repository)
                .add("team", team)
                .add("artifact", artifact)
                .add("tag", tag)
                .toString();
    }
}
