package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;
import com.amazonaws.services.ec2.model.InstanceAttribute;
import com.amazonaws.util.Base64;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.error.YAMLException;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.FetchTaupageYaml;
import org.zalando.stups.fullstop.taupage.TaupageYaml;
import org.zalando.stups.fullstop.taupage.TaupageYamlUtil;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class FetchTaupageYamlImpl implements FetchTaupageYaml {

    private static final String USER_DATA = "userData";
    private final ClientProvider clientProvider;
    private final Logger log = getLogger(getClass());

    @Autowired
    public FetchTaupageYamlImpl(final ClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public Optional<TaupageYaml> getTaupageYaml(final String instanceId, final String account, final String region) {
        final AmazonEC2Client client = clientProvider.getClient(AmazonEC2Client.class,
                account,
                Region.getRegion(Regions.fromName(region)));

        try {
            final DescribeInstanceAttributeResult response = client.describeInstanceAttribute(
                    new DescribeInstanceAttributeRequest()
                            .withInstanceId(instanceId)
                            .withAttribute(USER_DATA));


            return ofNullable(response)
                    .map(DescribeInstanceAttributeResult::getInstanceAttribute)
                    .map(InstanceAttribute::getUserData)
                    .map(Base64::decode)
                    .map(String::new)
                    .map(TaupageYamlUtil::parseTaupageYaml);

        } catch (final AmazonClientException e) {
            log.warn("Could not get Taupage YAML for instance: " + instanceId, e);
            return empty();
        } catch (YAMLException | IllegalArgumentException s) {
            log.warn("Taupage YAML is not valid for instance: " + instanceId, s);
            return empty();
        }
    }


}
