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
package org.zalando.stups.fullstop.jobs.ec2;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
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
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_ENDPOINT;

@Component
public class FetchEC2Job {

    private final Logger log = LoggerFactory.getLogger(FetchEC2Job.class);

    private ViolationSink violationSink;

    private ClientProvider clientProvider;

    private TeamOperations teamOperations;

    private JobsProperties jobsProperties;

    private SecurityGroupsChecker securityGroupsChecker;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    private CloseableHttpClient httpclient;

    private final AwsApplications awsApplications;

    @Autowired
    public FetchEC2Job(ViolationSink violationSink,
                       ClientProvider clientProvider,
                       TeamOperations teamOperations,
                       JobsProperties jobsProperties,
                       @Qualifier("ec2SecurityGroupsChecker") SecurityGroupsChecker securityGroupsChecker,
                       AwsApplications awsApplications) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.teamOperations = teamOperations;
        this.jobsProperties = jobsProperties;
        this.securityGroupsChecker = securityGroupsChecker;
        this.awsApplications = awsApplications;

        threadPoolTaskExecutor.setCorePoolSize(8);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        threadPoolTaskExecutor.setThreadGroupName("ec2-check-group");
        threadPoolTaskExecutor.setThreadNamePrefix("ec2-check-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.afterPropertiesSet();

        try {
            final RequestConfig config = RequestConfig.custom()
                    .setConnectionRequestTimeout(1000)
                    .setConnectTimeout(1000)
                    .setSocketTimeout(1000)
                    .build();
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
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("Could not initialize httpClient", e);
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
                final DescribeInstancesResult describeEC2Result = getDescribeEC2Result(
                        account,
                        region);

                for (final Reservation reservation : describeEC2Result.getReservations()) {

                    for (final Instance instance : reservation.getInstances()) {
                        final Map<String, Object> metaData = newHashMap();
                        final List<String> errorMessages = newArrayList();
                        final String instancePublicIpAddress = instance.getPublicIpAddress();

                        if (instancePublicIpAddress == null || instancePublicIpAddress.isEmpty()) {
                            continue;
                        }

                        final Set<String> unsecureGroups = securityGroupsChecker.check(
                                instance.getSecurityGroups().stream().map(GroupIdentifier::getGroupId).collect(toList()),
                                account,
                                getRegion(fromName(region)));
                        if (!unsecureGroups.isEmpty()) {
                            metaData.put("unsecuredSecurityGroups", unsecureGroups);
                            errorMessages.add("Unsecured security group! Only ports 80 and 443 are allowed");
                        }

                        if (metaData.size() > 0) {
                            metaData.put("errorMessages", errorMessages);
                            writeViolation(account, region, metaData, instance.getInstanceId());

                            // skip http response check, as we are already having a violation here
                            continue;
                        }

                        // skip check for publicly available apps
                        if (awsApplications.isPubliclyAccessible(account, region, newArrayList(instance.getInstanceId())).orElse(false)) {
                            continue;
                        }

                        for (Integer allowedPort : jobsProperties.getEc2AllowedPorts()) {

                            if (allowedPort == 22) {
                                continue;
                            }

                            EC2HttpCall httpCall = new EC2HttpCall(httpclient, instance, allowedPort);
                            ListenableFuture<Boolean> listenableFuture = threadPoolTaskExecutor.submitListenable(
                                    httpCall);
                            listenableFuture.addCallback(
                                    result -> {
                                        log.info("address: {} and port: {}", instancePublicIpAddress, allowedPort);
                                        if (!result) {
                                            Map<String, Object> md = newHashMap();
                                            md.put("instancePublicIpAddress", instancePublicIpAddress);
                                            md.put("allowedPort", allowedPort);
                                            writeViolation(account, region, md, instance.getInstanceId());
                                        }
                                    }, ex -> {
                                        log.warn(ex.getMessage(), ex);
                                        Map<String, Object> md = newHashMap();
                                        md.put("instancePublicIpAddress", instancePublicIpAddress);
                                        md.put("allowedPort", allowedPort);
                                        writeViolation(account, region, md, instance.getInstanceId());
                                    });

                            log.debug("getActiveCount: {}", threadPoolTaskExecutor.getActiveCount());
                            log.debug("### - Thread: {}", Thread.currentThread().getId());

                        }

                    }

                }

            }
        }

    }

    private void writeViolation(String account, String region, Object metaInfo, String instanceId) {
        ViolationBuilder violationBuilder = new ViolationBuilder();
        Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(FetchEC2Job.class)
                .withType(UNSECURED_ENDPOINT)
                .withMetaInfo(metaInfo)
                .withInstanceId(instanceId)
                .withEventId("checkPublicEC2InstanceJob").build();
        violationSink.put(violation);
    }

    private DescribeInstancesResult getDescribeEC2Result(String account, String region) {
        AmazonEC2Client ec2Client = clientProvider.getClient(
                AmazonEC2Client.class,
                account,
                getRegion(
                        fromName(region)));
        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
        describeInstancesRequest.setFilters(newArrayList(new Filter("ip-address", newArrayList("*"))));
        return ec2Client.describeInstances(describeInstancesRequest);
    }

    private List<String> fetchAccountIds() {
        List<String> accountIds = newArrayList();
        List<Account> accounts = teamOperations.getAccounts();
        accountIds.addAll(accounts.stream().map(Account::getId).collect(toList()));
        return accountIds;

    }
}
