package org.zalando.stups.fullstop.jobs.policy;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
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

    private final ViolationService violationService;

    @Autowired
    public CrossAccountPolicyForIAMJob(final ViolationSink violationSink,
                                       final ClientProvider clientProvider,
                                       final AccountIdSupplier allAccountIds,
                                       final JobsProperties jobsProperties,
                                       final ViolationService violationService) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
        this.jobsProperties = jobsProperties;
        this.violationService = violationService;
    }

    @PostConstruct
    public void init() {
        log.info("{} initalized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 300_000, initialDelay = 240_000) // 5 min rate, 4 min delay
    public void run() {
        log.info("Running job {}", getClass().getSimpleName());
        for (final String account : allAccountIds.get()) {
            for (final String region : jobsProperties.getWhitelistedRegions()) {

                final AmazonIdentityManagementClient iamClient = clientProvider.getClient(
                        AmazonIdentityManagementClient.class,
                        account,
                        getRegion(fromName(region)));

                final ListRolesResult listRolesResult = iamClient.listRoles();

                for (final Role role : listRolesResult.getRoles()) {

                    final String assumeRolePolicyDocument = role.getAssumeRolePolicyDocument();

                    final List<String> principalArns = JsonPath.read(assumeRolePolicyDocument, ".Statement[].Principal.AWS");

                    final List<String> crossAccountIds = principalArns.stream()
                            .filter(principalARN -> !principalARN.contains(account))
                            .collect(toList());

                    if (crossAccountIds != null && !crossAccountIds.isEmpty()) {
                        writeViolation(
                                account,
                                region,
                                ImmutableMap.of(
                                        "RoleArn", role.getArn(),
                                        "RoleName", role.getRoleName(),
                                        "arn", crossAccountIds),
                                role.getRoleId()
                        );
                    }
                }
            }
        }

        log.info("Completed job {}", getClass().getSimpleName());
    }

    private void writeViolation(final String account, final String region, final Object metaInfo, final String roleId) {
        final ViolationBuilder violationBuilder = new ViolationBuilder();
        final Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(CrossAccountPolicyForIAMJob.class)
                .withType(CROSS_ACCOUNT_ROLE)
                .withMetaInfo(metaInfo)
                .withInstanceId(roleId)
                .withEventId(EVENT_ID).build();
        violationSink.put(violation);
    }

}
