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
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;
import org.zalando.stups.fullstop.aws.AwsRequestUtil;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.TaupageYamlProvider;
import org.zalando.stups.fullstop.taupage.TaupageYaml;
import org.zalando.stups.fullstop.taupage.TaupageYamlUtil;

import javax.annotation.Nonnull;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class TaupageYamlProviderImpl implements TaupageYamlProvider {

    private static final String USER_DATA = "userData";

    private final Logger log = getLogger(getClass());

    private final LoadingCache<EC2InstanceContext, Optional<TaupageYaml>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<EC2InstanceContext, Optional<TaupageYaml>>() {
                @Override
                public Optional<TaupageYaml> load(@Nonnull final EC2InstanceContext context) throws Exception {
                    final Optional<TaupageYaml> taupageYaml = getTaupageYaml(context);
                    if (!taupageYaml.isPresent()) {
                        log.warn("Could not find the Taupage YAML for {}", context);
                    }
                    return taupageYaml;
                }
            });

    private Optional<TaupageYaml> getTaupageYaml(@Nonnull final EC2InstanceContext context) {

        if (context.isTaupageAmi().orElse(false)) {
            final String instanceId = context.getInstanceId();
            try {
                final AmazonEC2Client ec2Client = context.getClient(AmazonEC2Client.class);
                return Optional.of(AwsRequestUtil.performRequest(
                        () -> ec2Client.describeInstanceAttribute(new DescribeInstanceAttributeRequest()
                                .withInstanceId(instanceId)
                                .withAttribute(USER_DATA))))
                        .map(DescribeInstanceAttributeResult::getInstanceAttribute)
                        .map(InstanceAttribute::getUserData)
                        .map(Base64::decode)
                        .map(String::new)
                        .map(TaupageYamlUtil::parseTaupageYaml);

            } catch (final AmazonClientException e) {
                log.warn("Could not get Taupage YAML for instance: " + instanceId, e);
                return empty();
            } catch (ScannerException | ParserException | IllegalArgumentException s)   {
                log.warn("Taupage YAML is not valid for instance: " + instanceId, s);
                return empty();
            }

        } else {
            return empty();
        }

    }

    @Override
    public Optional<TaupageYaml> apply(final EC2InstanceContext context) {
        return cache.getUnchecked(context);
    }
}
