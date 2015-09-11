/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.jobs.elb;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
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
import org.zalando.stups.fullstop.jobs.common.AwsApplications;
import org.zalando.stups.fullstop.jobs.common.PortsChecker;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupsChecker;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.teams.Account;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import javax.annotation.PostConstruct;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_ENDPOINT;

/**
 * Created by gkneitschel.
 */
@Component
public class FetchElasticLoadBalancersJob {

    private final Logger log = LoggerFactory.getLogger(FetchElasticLoadBalancersJob.class);

    private final ViolationSink violationSink;

    private final ClientProvider clientProvider;

    private final TeamOperations teamOperations;

    private final JobsProperties jobsProperties;

    private SecurityGroupsChecker securityGroupsChecker;

    private final PortsChecker portsChecker;

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    private final CloseableHttpClient httpclient;

    private final AwsApplications awsApplications;

    @Autowired
    public FetchElasticLoadBalancersJob(ViolationSink violationSink,
                                        ClientProvider clientProvider,
                                        TeamOperations teamOperations,
                                        JobsProperties jobsProperties,
                                        @Qualifier("elbSecurityGroupsChecker") SecurityGroupsChecker securityGroupsChecker,
                                        PortsChecker portsChecker,
                                        AwsApplications awsApplications) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.teamOperations = teamOperations;
        this.jobsProperties = jobsProperties;
        this.securityGroupsChecker = securityGroupsChecker;
        this.portsChecker = portsChecker;
        this.awsApplications = awsApplications;

        threadPoolTaskExecutor.setCorePoolSize(8);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(100);
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
        }
        catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("Could not initialize httpClient", e);
        }
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 300_000)
    public void check() {
        List<String> accountIds = fetchAccountIds();
        log.info("Running job {} (found {} accounts)", getClass().getSimpleName(), accountIds.size());
        for (String account : accountIds) {
            for (String region : jobsProperties.getWhitelistedRegions()) {
                log.info("Scanning ELBs for {}/{}", account, region);
                for (LoadBalancerDescription elb : getELBs(account, region)) {
                    Map<String, Object> metaData = newHashMap();
                    List<String> errorMessages = newArrayList();
                    final String canonicalHostedZoneName = elb.getCanonicalHostedZoneName();

                    if (!elb.getScheme().equals("internet-facing")) {
                        continue;
                    }

                    List<Integer> unsecuredPorts = portsChecker.check(elb);
                    if (!unsecuredPorts.isEmpty()) {
                        metaData.put("unsecuredPorts", unsecuredPorts);
                        errorMessages.add(format("ELB %s listens on unsecure ports! Only ports 80 and 443 are allowed",
                                elb.getLoadBalancerName()));
                    }


                    final Set<String> unsecureGroups = securityGroupsChecker.check(
                            elb.getSecurityGroups(),
                            account,
                            getRegion(fromName(region)));
                    if (!unsecureGroups.isEmpty()) {
                        metaData.put("unsecuredSecurityGroups", unsecureGroups);
                        errorMessages.add("Unsecured security group! Only ports 80 and 443 are allowed");
                    }


                    if (metaData.size() > 0) {
                        metaData.put("errorMessages", errorMessages);
                        writeViolation(account, region, metaData, canonicalHostedZoneName);

                        // skip http response check, as we are already having a violation here
                        continue;
                    }

                    final List<String> instanceIds = elb.getInstances().stream().map(Instance::getInstanceId).collect(toList());

                    // skip check for publicly available apps
                    if (awsApplications.isPubliclyAccessible(account, region, instanceIds).orElse(false)) {
                        continue;
                    }


                    for (Integer allowedPort : jobsProperties.getElbAllowedPorts()) {

                        ELBHttpCall ELBHttpCall = new ELBHttpCall(httpclient, elb, allowedPort);
                        ListenableFuture<Boolean> listenableFuture = threadPoolTaskExecutor.submitListenable(ELBHttpCall);
                        listenableFuture.addCallback(
                                result -> {
                                    log.info("address: {} and port: {}", canonicalHostedZoneName, allowedPort);
                                    if (!result) {
                                        final Map<String, Object> md = newHashMap();
                                        md.put("canonicalHostedZoneName", canonicalHostedZoneName);
                                        md.put("allowedPort", allowedPort);
                                        writeViolation(account, region, md, canonicalHostedZoneName);
                                    }
                                }, ex -> {
                                    log.warn(ex.getMessage(), ex);
                                    Map<String, Object> md = newHashMap();
                                    md.put("canonicalHostedZoneName", canonicalHostedZoneName);
                                    md.put("allowedPort", allowedPort);
                                    writeViolation(account, region, md, canonicalHostedZoneName);
                                });

                        log.debug("getActiveCount: {}", threadPoolTaskExecutor.getActiveCount());
                        log.debug("### - Thread: {}", Thread.currentThread().getId());

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
                                              .withType(UNSECURED_ENDPOINT)
                                              .withMetaInfo(metaInfo)
                                              .withEventId(canonicalHostedZoneName).build();
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

    private List<String> fetchAccountIds() {
        List<String> accountIds = newArrayList();
        List<Account> accounts = teamOperations.getAccounts();
        accountIds.addAll(accounts.stream().map(Account::getId).collect(toList()));
        return accountIds;

    }
}
