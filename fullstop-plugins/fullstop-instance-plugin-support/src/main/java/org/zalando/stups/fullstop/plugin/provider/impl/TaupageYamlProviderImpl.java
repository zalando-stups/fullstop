package org.zalando.stups.fullstop.plugin.provider.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.InstanceAttribute;
import com.amazonaws.util.Base64;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.TaupageYamlProvider;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.*;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class TaupageYamlProviderImpl implements TaupageYamlProvider {

    public static final String USER_DATA = "userData";

    private final Logger log = getLogger(getClass());

    private final LoadingCache<EC2InstanceContext, Optional<Map>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<Map>>() {
                @Override
                public Optional<Map> load(@Nonnull EC2InstanceContext context) throws Exception {
                    final Optional<Map> taupageYaml = getTaupageYaml(context);
                    if (!taupageYaml.isPresent()) {
                        log.warn("Could not find the Taupage YAML for {}", context);
                    }
                    return taupageYaml;
                }
            });

    private Optional<Map> getTaupageYaml(@Nonnull EC2InstanceContext context) {

        if (context.isTaupageAmi().orElse(false)) {

            final String instanceId = context.getInstanceId();

            try {
                DescribeInstanceAttributeResult response = context.getClient(AmazonEC2Client.class)
                        .describeInstanceAttribute(
                                new DescribeInstanceAttributeRequest()
                                        .withInstanceId(instanceId)
                                        .withAttribute(USER_DATA));

                return ofNullable(response)
                        .map(DescribeInstanceAttributeResult::getInstanceAttribute)
                        .map(InstanceAttribute::getUserData)
                        .map(Base64::decode)
                        .map(String::new)
                        .map(data -> (Map) new Yaml().load(data));

            } catch (AmazonClientException e) {
                log.warn("Could not get Taupage YAML for instance: " + instanceId, e);
                return empty();
            }

        } else {
            return empty();
        }

    }

    @Override
    public Optional<Map> apply(EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }
}
