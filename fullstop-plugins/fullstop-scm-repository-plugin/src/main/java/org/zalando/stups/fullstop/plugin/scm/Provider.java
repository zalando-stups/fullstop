package org.zalando.stups.fullstop.plugin.scm;

public enum Provider {

    GITHUB("https://%s/%s/%s",
            "^(?:git:)?(?:git@|https:\\/\\/)(?<host>%s)(?::\\d+)?(?::|\\/)(?<owner>.+)(?:\\/)(?<name>.+)(?:\\.git)?$"),

    // TODO figure out the proper regex
    STASH("https://%s/scm/%s/%s",
            "(?:git:)?(?:ssh:\\/\\/|https:\\/\\/)(?:[\\d\\w]+@)?(?<host>%s)(?:\\/|\\:[\\d]+)?\\/([^\\/]+)\\/([^\\/.]+)(?:\\.git)?");

    private final String normalizedUrlFormat;
    private final String urlRegexTemplate;

    Provider(String normalizedUrlFormat, String urlRegexTemplate) {
        this.normalizedUrlFormat = normalizedUrlFormat;
        this.urlRegexTemplate = urlRegexTemplate;
    }

    public String getNormalizedUrlFormat() {
        return normalizedUrlFormat;
    }

    public String getUrlRegexTemplate() {
        return urlRegexTemplate;
    }
}
