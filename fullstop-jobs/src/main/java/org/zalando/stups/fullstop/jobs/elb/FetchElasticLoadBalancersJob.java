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

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.zalando.stups.fullstop.aws.ClientProvider;
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
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_ENDPOINT;

/**
 * Created by gkneitschel.
 */
@Component
public class FetchElasticLoadBalancersJob {

    private final Logger log = LoggerFactory.getLogger(FetchElasticLoadBalancersJob.class);

    private ViolationSink violationSink;

    private ClientProvider clientProvider;

    private TeamOperations teamOperations;

    private JobsProperties jobsProperties;

    private SecurityGroupsChecker securityGroupsChecker;

    private PortsChecker portsChecker;

    private Set<Integer> allowedPorts = newHashSet(443, 80);

    private ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    private RequestConfig config = RequestConfig.custom()
                                                .setConnectionRequestTimeout(1000)
                                                .setConnectTimeout(1000)
                                                .setSocketTimeout(1000)
                                                .build();

    private CloseableHttpClient httpclient;

    @Autowired
    public FetchElasticLoadBalancersJob(ViolationSink violationSink,
            ClientProvider clientProvider, TeamOperations teamOperations, JobsProperties jobsProperties /*,
            SecurityGroupsChecker securityGroupsChecker*/, PortsChecker portsChecker) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.teamOperations = teamOperations;
        this.jobsProperties = jobsProperties;
        this.securityGroupsChecker = null; //securityGroupsChecker;
        this.portsChecker = portsChecker;

        threadPoolTaskExecutor.setCorePoolSize(8);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        threadPoolTaskExecutor.setThreadGroupName("elb-check-group");
        threadPoolTaskExecutor.setThreadNamePrefix("elb-check-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setDaemon(true);
        threadPoolTaskExecutor.afterPropertiesSet();

        try {
            httpclient = HttpClientBuilder.create()
                                          .disableAuthCaching()
                                          .disableAutomaticRetries()
                                          .disableConnectionState()
                                          .disableCookieManagement()
                                          .disableRedirectHandling()
                                          .setDefaultRequestConfig(config)
                                          .setHostnameVerifier(new AllowAllHostnameVerifier())
                                          .setSslcontext(
                                                  new SSLContextBuilder()
                                                          .loadTrustMaterial(
                                                                  null,
                                                                  (arrayX509Certificate, value) -> true)
                                                          .build())
                                          .build();
        }
        catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
            // TODO: handle this!!!
        }
    }

    @PostConstruct
    public void init() {
        log.info("{} initalized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 300_000)
    public void check() {
        List<String> accountIds = fetchAccountIds();
        log.info("Running job {} (found {} accounts)", getClass().getSimpleName(), accountIds.size());
        for (String account : accountIds) {
            for (String region : jobsProperties.getWhitelistedRegions()) {
                log.info("Scanning ELBs for {}/{}", account, region);
                DescribeLoadBalancersResult describeLoadBalancersResult = getDescribeLoadBalancersResult(
                        account,
                        region);

                for (LoadBalancerDescription loadBalancerDescription : describeLoadBalancersResult.getLoadBalancerDescriptions()) {
                    Map<String, Object> metaData = newHashMap();
                    List<String> errorMessages = newArrayList();
                    final String canonicalHostedZoneName = loadBalancerDescription.getCanonicalHostedZoneName();

                    if (!loadBalancerDescription.getScheme().equals("internet-facing")) {
                        continue;
                    }

                    List<Integer> unsecuredPorts = portsChecker.check(loadBalancerDescription);
                    if (!unsecuredPorts.isEmpty()) {
                        metaData.put("unsecuredPorts", unsecuredPorts);
                        errorMessages.add(
                                String.format(
                                        "ELB %s listens on unsecure ports! Only ports 80 and 443 are allowed",
                                        loadBalancerDescription.getLoadBalancerName()));
                    }

                    /*
                    Set<String> unsecureGroups = securityGroupsChecker.check(
                            newHashSet(loadBalancerDescription.getSecurityGroups()),
                            account,
                            Region.getRegion(Regions.fromName(region)));
                    if (!unsecureGroups.isEmpty()) {
                        metaData.put("unsecuredSecurityGroups", unsecureGroups);
                        errorMessages.add("Unsecured security group! Only ports 80 and 443 are allowed");
                    }
                    */

                    if (metaData.size() > 0) {
                        metaData.put("errorMessages", errorMessages);
                        writeViolation(account, region, metaData, canonicalHostedZoneName);
                    }

                    for (Integer allowedPort : allowedPorts) {

                        HttpCall httpCall = new HttpCall(httpclient, loadBalancerDescription, allowedPort);
                        ListenableFuture<Boolean> listenableFuture = threadPoolTaskExecutor.submitListenable(httpCall);
                        listenableFuture.addCallback(
                                new SuccessCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        log.info("address: {} and port: {}", canonicalHostedZoneName, allowedPort);
                                        if (!result) {
                                            violationSink.put(
                                                    new ViolationBuilder().withType(UNSECURED_ENDPOINT)
                                                                          .withPluginFullyQualifiedClassName(
                                                                                  FetchElasticLoadBalancersJob.class)
                                                                          .withMetaInfo(
                                                                                  newArrayList(
                                                                                          canonicalHostedZoneName,
                                                                                          allowedPort))
                                                                          .build());
                                        }
                                    }
                                }, new FailureCallback() {
                                    @Override
                                    public void onFailure(Throwable ex) {
                                        log.warn(ex.getMessage(), ex);
                                        violationSink.put(
                                                new ViolationBuilder().withType(UNSECURED_ENDPOINT)
                                                                      .withPluginFullyQualifiedClassName(
                                                                              FetchElasticLoadBalancersJob.class)
                                                                      .withMetaInfo(
                                                                              newArrayList(
                                                                                      canonicalHostedZoneName,
                                                                                      allowedPort))
                                                                      .build());
                                    }
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

    private DescribeLoadBalancersResult getDescribeLoadBalancersResult(String account, String region) {
        AmazonElasticLoadBalancingClient elbClient = clientProvider.getClient(
                AmazonElasticLoadBalancingClient.class,
                account,
                Region.getRegion(
                        Regions.fromName(region)));
        DescribeLoadBalancersRequest describeLoadBalancersRequest = new DescribeLoadBalancersRequest();
        return elbClient.describeLoadBalancers(
                describeLoadBalancersRequest);
    }

    private List<String> fetchAccountIds() {
        List<String> accountIds = newArrayList();
        List<Account> accounts = teamOperations.getAccounts();
        accountIds.addAll(accounts.stream().map(Account::getId).collect(Collectors.toList()));
        return accountIds;

    }
}
