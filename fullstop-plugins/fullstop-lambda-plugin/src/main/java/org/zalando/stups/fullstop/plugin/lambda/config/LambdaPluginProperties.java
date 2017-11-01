package org.zalando.stups.fullstop.plugin.lambda.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


@ConfigurationProperties(prefix = "fullstop.plugins.lambda")
public class LambdaPluginProperties {

    private String s3Buckets;

    public List<String> getS3Buckets() {
        return Stream.of(s3Buckets.split(","))
                .collect(toList());
    }

    public void setS3Buckets(final String s3Buckets) {
        this.s3Buckets = s3Buckets;
    }
}
