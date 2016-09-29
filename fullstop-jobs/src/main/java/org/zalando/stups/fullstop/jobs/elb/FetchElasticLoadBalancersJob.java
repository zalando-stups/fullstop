package org.zalando.stups.fullstop.jobs.elb;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
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
import org.zalando.stups.fullstop.jobs.common.EC2InstanceProvider;
import org.zalando.stups.fullstop.jobs.common.FetchTaupageYaml;
import org.zalando.stups.fullstop.jobs.common.HttpCallResult;
import org.zalando.stups.fullstop.jobs.common.HttpGetRootCall;
import org.zalando.stups.fullstop.jobs.common.PortsChecker;
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
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_PUBLIC_ENDPOINT;

/**
 * Created by gkneitschel.
 */
@Component
public class FetchElasticLoadBalancersJob implements FullstopJob {

    private static final String EVENT_ID = "checkElbJob";

    private final Logger log = LoggerFactory.getLogger(FetchElasticLoadBalancersJob.class);

    private final ViolationSink violationSink;

    private final ClientProvider clientProvider;

    private final AccountIdSupplier allAccountIds;

    private final JobsProperties jobsProperties;

    private final SecurityGroupsChecker securityGroupsChecker;

    private final PortsChecker portsChecker;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    private final CloseableHttpClient httpclient;

    private final AwsApplications awsApplications;

    private final ViolationService violationService;

    private final FetchTaupageYaml fetchTaupageYaml;

    private final AmiDetailsProvider amiDetailsProvider;

    private final EC2InstanceProvider ec2Instance;

    @Autowired
    public FetchElasticLoadBalancersJob(final ViolationSink violationSink,
                                        final ClientProvider clientProvider,
                                        final AccountIdSupplier allAccountIds, final JobsProperties jobsProperties,
                                        @Qualifier("elbSecurityGroupsChecker") final SecurityGroupsChecker securityGroupsChecker,
                                        final PortsChecker portsChecker,
                                        final AwsApplications awsApplications,
                                        final ViolationService violationService,
                                        final FetchTaupageYaml fetchTaupageYaml,
                                        final AmiDetailsProvider amiDetailsProvider,
                                        final EC2InstanceProvider ec2Instance,
                                        final CloseableHttpClient httpClient) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
        this.jobsProperties = jobsProperties;
        this.securityGroupsChecker = securityGroupsChecker;
        this.portsChecker = portsChecker;
        this.awsApplications = awsApplications;
        this.violationService = violationService;
        this.fetchTaupageYaml = fetchTaupageYaml;
        this.amiDetailsProvider = amiDetailsProvider;
        this.ec2Instance = ec2Instance;
        this.httpclient = httpClient;

        threadPoolTaskExecutor.setCorePoolSize(12);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(75);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        threadPoolTaskExecutor.setThreadGroupName("elb-check-group");
        threadPoolTaskExecutor.setThreadNamePrefix("elb-check-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.afterPropertiesSet();
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 300_000, initialDelay = 120_000) // 5 min rate, 2 min delay
    public void run() {
        log.info("Running job {}", getClass().getSimpleName());
        for (final String account : allAccountIds.get()) {
            for (final String region : jobsProperties.getWhitelistedRegions()) {
                log.info("Scanning ELBs for {}/{}", account, region);

                try {
                    final Region awsRegion = getRegion(fromName(region));
                    final AmazonElasticLoadBalancingClient elbClient = clientProvider.getClient(
                            AmazonElasticLoadBalancingClient.class,
                            account,
                            getRegion(fromName(region)));

                    Optional<String> marker = Optional.empty();

                    do {
                        final DescribeLoadBalancersRequest request = new DescribeLoadBalancersRequest();
                        marker.ifPresent(request::setMarker);
                        final DescribeLoadBalancersResult result = elbClient.describeLoadBalancers(request);
                        marker = Optional.ofNullable(StringUtils.trimToNull(result.getNextMarker()));

                        for (final LoadBalancerDescription elb : result.getLoadBalancerDescriptions()) {
                            processELB(account, awsRegion, elb);
                        }

                    } while (marker.isPresent());

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

    private boolean processELB(String account, Region awsRegion, LoadBalancerDescription elb) {
        final Map<String, Object> metaData = newHashMap();
        final List<String> errorMessages = newArrayList();
        final String canonicalHostedZoneName = elb.getCanonicalHostedZoneName();

        final List<String> instanceIds = elb.getInstances().stream().map(Instance::getInstanceId).collect(toList());

        instanceIds.stream()
                .map(id -> ec2Instance.getById(account, awsRegion, id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(com.amazonaws.services.ec2.model.Instance::getImageId)
                .map(amiId -> amiDetailsProvider.getAmiDetails(account, awsRegion, amiId))
                .findFirst()
                .ifPresent(metaData::putAll);


        if (!elb.getScheme().equals("internet-facing")) {
            return true;
        }

        if (violationService.violationExists(account, awsRegion.getName(), EVENT_ID, canonicalHostedZoneName, UNSECURED_PUBLIC_ENDPOINT)) {
            return true;
        }

        final List<Integer> unsecuredPorts = portsChecker.check(elb);
        if (!unsecuredPorts.isEmpty()) {
            metaData.put("unsecuredPorts", unsecuredPorts);
            errorMessages.add(format("ELB %s listens on insecure ports! Only ports 80 and 443 are allowed",
                    elb.getLoadBalancerName()));
        }


        final Set<String> unsecureGroups = securityGroupsChecker.check(
                elb.getSecurityGroups(),
                account,
                awsRegion);
        if (!unsecureGroups.isEmpty()) {
            metaData.put("unsecuredSecurityGroups", unsecureGroups);
            errorMessages.add("Unsecured security group! Only ports 80 and 443 are allowed");
        }


        if (errorMessages.size() > 0) {
            metaData.put("errorMessages", errorMessages);
            writeViolation(account, awsRegion.getName(), metaData, canonicalHostedZoneName, instanceIds);

            // skip http response check, as we are already having a violation here
            return true;
        }


        // skip check for publicly available apps
        if (awsApplications.isPubliclyAccessible(account, awsRegion.getName(), instanceIds).orElse(false)) {
            return true;
        }

        for (final Integer allowedPort : jobsProperties.getElbAllowedPorts()) {
            final HttpGetRootCall HttpGetRootCall = new HttpGetRootCall(httpclient, canonicalHostedZoneName, allowedPort);
            final ListenableFuture<HttpCallResult> listenableFuture = threadPoolTaskExecutor.submitListenable(HttpGetRootCall);
            listenableFuture.addCallback(
                    httpCallResult -> {
                        log.info("address: {} and port: {}", canonicalHostedZoneName, allowedPort);
                        if (httpCallResult.isOpen()) {
                            final Map<String, Object> md = ImmutableMap.<String, Object>builder()
                                    .putAll(metaData)
                                    .put("canonicalHostedZoneName", canonicalHostedZoneName)
                                    .put("port", allowedPort)
                                    .put("Error", httpCallResult.getMessage())
                                    .build();
                            writeViolation(account, awsRegion.getName(), md, canonicalHostedZoneName, instanceIds);
                        }
                    }, ex -> log.warn(ex.getMessage(), ex));

            log.debug("Active threads in pool: {}/{}", threadPoolTaskExecutor.getActiveCount(), threadPoolTaskExecutor.getMaxPoolSize());
        }
        return false;
    }

    private void writeViolation(final String account, final String region, final Object metaInfo, final String canonicalHostedZoneName, final List<String> instanceIds) {

        final Optional<TaupageYaml> taupageYaml = instanceIds.
                stream().
                map(id -> fetchTaupageYaml.getTaupageYaml(id, account, region)).
                filter(Optional::isPresent).
                map(Optional::get).
                findFirst();


        final ViolationBuilder violationBuilder = new ViolationBuilder();
        final Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(FetchElasticLoadBalancersJob.class)
                .withType(UNSECURED_PUBLIC_ENDPOINT)
                .withMetaInfo(metaInfo)
                .withEventId(EVENT_ID)
                .withInstanceId(canonicalHostedZoneName)
                .withApplicationId(taupageYaml.map(TaupageYaml::getApplicationId).map(StringUtils::trimToNull).orElse(null))
                .withApplicationVersion(taupageYaml.map(TaupageYaml::getApplicationVersion).map(StringUtils::trimToNull).orElse(null))
                .build();
        violationSink.put(violation);
    }
}
