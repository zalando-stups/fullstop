package org.zalando.stups.fullstop.jobs.ec2;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.common.AmiDetailsProvider;
import org.zalando.stups.fullstop.jobs.common.AwsApplications;
import org.zalando.stups.fullstop.jobs.common.FetchTaupageYaml;
import org.zalando.stups.fullstop.jobs.common.HttpCallResult;
import org.zalando.stups.fullstop.jobs.common.HttpGetRootCall;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.taupage.TaupageYaml;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_PUBLIC_ENDPOINT;

@Component
public class FetchEC2Job implements FullstopJob {

    private static final String EVENT_ID = "checkPublicEC2InstanceJob";

    private final Logger log = LoggerFactory.getLogger(FetchEC2Job.class);

    private final ViolationSink violationSink;

    private final ClientProvider clientProvider;

    private final AccountIdSupplier allAccountIds;

    private final JobsProperties jobsProperties;

    private final SecurityGroupsChecker securityGroupsChecker;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    private final CloseableHttpClient httpClient;

    private final AwsApplications awsApplications;

    private final ViolationService violationService;

    private final FetchTaupageYaml fetchTaupageYaml;

    private final AmiDetailsProvider amiDetailsProvider;

    @Autowired
    public FetchEC2Job(final ViolationSink violationSink,
                       final ClientProvider clientProvider,
                       final AccountIdSupplier allAccountIds,
                       final JobsProperties jobsProperties,
                       final @Qualifier("ec2SecurityGroupsChecker") SecurityGroupsChecker securityGroupsChecker,
                       final AwsApplications awsApplications,
                       final ViolationService violationService,
                       final FetchTaupageYaml fetchTaupageYaml,
                       final AmiDetailsProvider amiDetailsProvider,
                       final CloseableHttpClient httpClient) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
        this.jobsProperties = jobsProperties;
        this.securityGroupsChecker = securityGroupsChecker;
        this.awsApplications = awsApplications;
        this.violationService = violationService;
        this.fetchTaupageYaml = fetchTaupageYaml;
        this.amiDetailsProvider = amiDetailsProvider;
        this.httpClient = httpClient;

        threadPoolTaskExecutor.setCorePoolSize(12);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(75);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        threadPoolTaskExecutor.setThreadGroupName("ec2-check-group");
        threadPoolTaskExecutor.setThreadNamePrefix("ec2-check-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.afterPropertiesSet();
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
                try {
                    log.info("Scanning public EC2 instances for {}/{}", account, region);
                    final AmazonEC2Client ec2Client = clientProvider.getClient(
                            AmazonEC2Client.class,
                            account,
                            getRegion(fromName(region)));
                    Optional<String> nextToken = empty();
                    do {
                        final DescribeInstancesRequest request = new DescribeInstancesRequest();
                        if (nextToken.isPresent()) {
                            request.setNextToken(nextToken.get());
                        } else {
                            request.setFilters(newArrayList(new Filter("ip-address", newArrayList("*"))));
                        }

                        final DescribeInstancesResult result = ec2Client.describeInstances(request);
                        nextToken = Optional.ofNullable(result.getNextToken());

                        for (final Reservation reservation : result.getReservations()) {
                            for (final Instance instance : reservation.getInstances()) {
                                processInstance(account, region, instance);
                            }
                        }
                    } while (nextToken.isPresent());

                } catch (final AmazonServiceException a) {

                    if (a.getErrorCode().equals("RequestLimitExceeded")) {
                        log.warn("RequestLimitExceeded for account: {}", account);
                    } else {
                        log.error(a.getMessage(), a);
                    }

                }
            }
        }
    }

    private void processInstance(String account, String region, Instance instance) {
        final Map<String, Object> metaData = newHashMap();
        metaData.putAll(amiDetailsProvider.getAmiDetails(account, getRegion(fromName(region)), instance.getImageId()));
        final List<String> errorMessages = newArrayList();
        final String instancePublicIpAddress = instance.getPublicIpAddress();

        if (violationService.violationExists(account, region, EVENT_ID, instance.getInstanceId(), UNSECURED_PUBLIC_ENDPOINT)) {
            return;
        }

        final Set<String> unsecureGroups = securityGroupsChecker.check(
                instance.getSecurityGroups().stream().map(GroupIdentifier::getGroupId).collect(toList()),
                account,
                getRegion(fromName(region)));
        if (!unsecureGroups.isEmpty()) {
            metaData.put("unsecuredSecurityGroups", unsecureGroups);
            errorMessages.add("Unsecured security group! Only ports 80 and 443 are allowed");
        }

        if (errorMessages.size() > 0) {
            metaData.put("errorMessages", errorMessages);
            writeViolation(account, region, metaData, instance.getInstanceId());

            // skip http response check, as we are already having a violation here
            return;
        }

        // skip check for publicly available apps
        if (awsApplications.isPubliclyAccessible(account, region, newArrayList(instance.getInstanceId())).orElse(false)) {
            return;
        }

        for (final Integer allowedPort : jobsProperties.getEc2AllowedPorts()) {

            if (allowedPort == 22) {
                continue;
            }

            final HttpGetRootCall httpCall = new HttpGetRootCall(httpClient, instancePublicIpAddress, allowedPort);
            final ListenableFuture<HttpCallResult> listenableFuture = threadPoolTaskExecutor.submitListenable(
                    httpCall);
            listenableFuture.addCallback(
                    httpCallResult -> {
                        log.info("address: {} and port: {}", instancePublicIpAddress, allowedPort);
                        if (httpCallResult.isOpen()) {
                            final Map<String, Object> md = ImmutableMap.<String, Object>builder()
                                    .putAll(metaData)
                                    .put("instancePublicIpAddress", instancePublicIpAddress)
                                    .put("Port", allowedPort)
                                    .put("Error", httpCallResult.getMessage()).build();
                            writeViolation(account, region, md, instance.getInstanceId());
                        }
                    }, ex -> log.warn("Could not call " + instancePublicIpAddress, ex));

            log.debug("Active threads in pool: {}/{}", threadPoolTaskExecutor.getActiveCount(), threadPoolTaskExecutor.getMaxPoolSize());
        }
    }

    private void writeViolation(final String account, final String region, final Object metaInfo, final String instanceId) {
        final Optional<TaupageYaml> taupageYaml = fetchTaupageYaml.getTaupageYaml(instanceId, account, region);

        final ViolationBuilder violationBuilder = new ViolationBuilder();
        final Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(FetchEC2Job.class)
                .withType(UNSECURED_PUBLIC_ENDPOINT)
                .withMetaInfo(metaInfo)
                .withInstanceId(instanceId)
                .withApplicationId(taupageYaml.map(TaupageYaml::getApplicationId).map(StringUtils::trimToNull).orElse(null))
                .withApplicationVersion(taupageYaml.map(TaupageYaml::getApplicationVersion).map(StringUtils::trimToNull).orElse(null))
                .withEventId(EVENT_ID).build();
        violationSink.put(violation);
    }
}
