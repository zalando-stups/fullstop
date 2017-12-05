package org.zalando.stups.fullstop.plugin.lambda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;


@ConfigurationProperties(prefix = "fullstop.plugins.lambda")
public class LambdaPluginProperties {

    private List<String> s3Buckets = newArrayList();

    public List<String> getS3Buckets() {
        return s3Buckets;
    }

    public void setS3Buckets(List<String> s3Buckets) {
        this.s3Buckets = s3Buckets;
    }
}
