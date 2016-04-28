package org.zalando.stups.fullstop.jobs.elb;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.zalando.stups.fullstop.jobs.common.HttpCallResult;
import org.zalando.stups.fullstop.jobs.common.HttpGetRootCall;
import org.zalando.stups.fullstop.jobs.common.PortsChecker;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import javax.annotation.PostConstruct;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

    private SecurityGroupsChecker securityGroupsChecker;

    private final PortsChecker portsChecker;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    private final CloseableHttpClient httpclient;

    private final AwsApplications awsApplications;

    private final ViolationService violationService;

    private final AmiDetailsProvider amiDetailsProvider;

    private final EC2InstanceProvider ec2Instance;

    @Autowired
    public FetchElasticLoadBalancersJob(ViolationSink violationSink,
                                        ClientProvider clientProvider,
                                        AccountIdSupplier allAccountIds, JobsProperties jobsProperties,
                                        @Qualifier("elbSecurityGroupsChecker") SecurityGroupsChecker securityGroupsChecker,
                                        PortsChecker portsChecker,
                                        AwsApplications awsApplications,
                                        ViolationService violationService,
                                        AmiDetailsProvider amiDetailsProvider,
                                        EC2InstanceProvider ec2Instance) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
        this.jobsProperties = jobsProperties;
        this.securityGroupsChecker = securityGroupsChecker;
        this.portsChecker = portsChecker;
        this.awsApplications = awsApplications;
        this.violationService = violationService;
        this.amiDetailsProvider = amiDetailsProvider;
        this.ec2Instance = ec2Instance;

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

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(1000)
                .setConnectTimeout(1000)
                .setSocketTimeout(1000)
                .build();
        try {
            httpclient = HttpClientBuilder.create()
                    .disableAuthCaching()
                    .disableAutomaticRetries()
                    .disableConnectionState()
                    .disableCookieManagement()
                    .disableRedirectHandling()
                    .setDefaultRequestConfig(requestConfig)
                    .setHostnameVerifier(new AllowAllHostnameVerifier())
                    .setSslcontext(
                            new SSLContextBuilder()
                                    .loadTrustMaterial(
                                            null,
                                            (arrayX509Certificate, value) -> true)
                                    .build())
                    .build();
        } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("Could not initialize httpClient", e);
        }
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 300_000, initialDelay = 120_000) // 5 min rate, 2 min delay
    public void run() {
        log.info("Running job {}", getClass().getSimpleName());
        for (String account : allAccountIds.get()) {
            for (String region : jobsProperties.getWhitelistedRegions()) {
                log.info("Scanning ELBs for {}/{}", account, region);

                try {
                    final Region awsRegion = getRegion(fromName(region));

                    for (LoadBalancerDescription elb : getELBs(account, region)) {
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
                            continue;
                        }

                        if (violationService.violationExists(account, region, EVENT_ID, canonicalHostedZoneName, UNSECURED_PUBLIC_ENDPOINT)) {
                            continue;
                        }

                        List<Integer> unsecuredPorts = portsChecker.check(elb);
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
                            writeViolation(account, region, metaData, canonicalHostedZoneName);

                            // skip http response check, as we are already having a violation here
                            continue;
                        }


                        // skip check for publicly available apps
                        if (awsApplications.isPubliclyAccessible(account, region, instanceIds).orElse(false)) {
                            continue;
                        }

                        for (Integer allowedPort : jobsProperties.getElbAllowedPorts()) {
                            HttpGetRootCall HttpGetRootCall = new HttpGetRootCall(httpclient, canonicalHostedZoneName, allowedPort);
                            ListenableFuture<HttpCallResult> listenableFuture = threadPoolTaskExecutor.submitListenable(HttpGetRootCall);
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
                                            writeViolation(account, region, md, canonicalHostedZoneName);
                                        }
                                    }, ex -> log.warn(ex.getMessage(), ex));

                            log.debug("Active threads in pool: {}/{}", threadPoolTaskExecutor.getActiveCount(), threadPoolTaskExecutor.getMaxPoolSize());
                        }

                    }

                } catch (AmazonServiceException a) {

                    if (a.getErrorCode().equals("RequestLimitExceeded")) {
                        log.warn("RequestLimitExceeded for account: {}", account);
                    } else {
                        log.error(a.getMessage(), a);
                    }

                }

            }

        }

    }

    private void writeViolation(String account, String region, Object metaInfo, String canonicalHostedZoneName) {
        ViolationBuilder violationBuilder = new ViolationBuilder();
        Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(FetchElasticLoadBalancersJob.class)
                .withType(UNSECURED_PUBLIC_ENDPOINT)
                .withMetaInfo(metaInfo)
                .withEventId(EVENT_ID)
                .withInstanceId(canonicalHostedZoneName)
                .build();
        violationSink.put(violation);
    }

    private List<LoadBalancerDescription> getELBs(String account, String region) {
        AmazonElasticLoadBalancingClient elbClient = clientProvider.getClient(
                AmazonElasticLoadBalancingClient.class,
                account,
                getRegion(
                        fromName(region)));


        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest();
        return elbClient.describeLoadBalancers(
                describeLoadBalancersRequest).getLoadBalancerDescriptions();
    }
}
