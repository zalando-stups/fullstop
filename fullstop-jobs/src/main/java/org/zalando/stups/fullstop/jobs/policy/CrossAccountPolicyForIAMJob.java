package org.zalando.stups.fullstop.jobs.policy;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.AwsRequestUtil;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.EU_WEST_1;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.violation.ViolationType.CROSS_ACCOUNT_ROLE;

@Component
public class CrossAccountPolicyForIAMJob implements FullstopJob {


    private static final String EVENT_ID = "crossAccountPolicyForIAMJob";

    private final Logger log = LoggerFactory.getLogger(CrossAccountPolicyForIAMJob.class);

    private final ViolationSink violationSink;

    private final ClientProvider clientProvider;

    private final AccountIdSupplier allAccountIds;

    private final JobsProperties jobsProperties;

    @Autowired
    public CrossAccountPolicyForIAMJob(final ViolationSink violationSink,
                                       final ClientProvider clientProvider,
                                       final AccountIdSupplier allAccountIds,
                                       final JobsProperties jobsProperties) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
        this.jobsProperties = jobsProperties;
    }

    @PostConstruct
    public void init() {
        log.info("{} initalized", getClass().getSimpleName());
    }

    @Scheduled(
            fixedRate = 1000 * 60 * 150, // 2.5 hours
            initialDelay = 1000 * 60 * 15 // 15 minutes
    )
    public void run() {
        log.info("Running job {}", getClass().getSimpleName());
        for (final String account : allAccountIds.get()) {

            final ListRolesResult listRolesResult = getListRolesResult(account);

            for (final Role role : listRolesResult.getRoles()) {

                final String assumeRolePolicyDocument = role.getAssumeRolePolicyDocument();

                List<String> principalArns = Lists.newArrayList();
                try {
                    principalArns = JsonPath.read(URLDecoder.decode(assumeRolePolicyDocument, "UTF-8"),
                            ".Statement[*].Principal.AWS");
                } catch (final UnsupportedEncodingException e) {
                    log.warn("Could not decode assumeRolePolicyDocument", e);
                }

                final List<String> crossAccountIds = principalArns.stream()
                        .filter(principalARN -> !principalARN.contains(account))
                        .filter(principalARN -> !principalARN.contains(jobsProperties.getManagementAccount()))
                        .collect(toList());

                if (crossAccountIds != null && !crossAccountIds.isEmpty()) {
                    writeViolation(
                            account,
                            ImmutableMap.of(
                                    "role_arn", role.getArn(),
                                    "role_name", role.getRoleName(),
                                    "grantees", crossAccountIds),
                            role.getRoleId()
                    );
                }
            }
        }

        log.info("Completed job {}", getClass().getSimpleName());
    }

    private ListRolesResult getListRolesResult(final String account) {
        final AmazonIdentityManagementClient iamClient = clientProvider.getClient(
                AmazonIdentityManagementClient.class,
                account,
                getRegion(EU_WEST_1)
        );

        return AwsRequestUtil.performRequest(iamClient::listRoles);
    }

    private void writeViolation(final String account, final Object metaInfo, final String roleId) {
        final ViolationBuilder violationBuilder = new ViolationBuilder();
        final Violation violation = violationBuilder.withAccountId(account)
                .withRegion(EU_WEST_1.getName())
                .withPluginFullyQualifiedClassName(CrossAccountPolicyForIAMJob.class)
                .withType(CROSS_ACCOUNT_ROLE)
                .withMetaInfo(metaInfo)
                .withInstanceId(roleId)
                .withEventId(EVENT_ID).build();
        violationSink.put(violation);
    }

}
