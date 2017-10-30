package org.zalando.stups.fullstop.plugin.lambda.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;


@ConfigurationProperties(prefix = "fullstop.plugins.lambda")
public class LambdaPluginProperties {

    private Set s3Buckets = newHashSet("zalando-lambda-repository-eu-central-1",
            "zalando-lambda-repository-eu-west-1");;

    public Set getS3Buckets() {
        return s3Buckets;
    }
}
