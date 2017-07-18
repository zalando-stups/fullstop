package org.zalando.stups.fullstop.jobs.rds;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.exception.JobExceptionHandler;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.zalando.stups.fullstop.violation.ViolationType.UNSECURED_PUBLIC_ENDPOINT;

@Component
public class FetchRdsJob implements FullstopJob {


    private static final String EVENT_ID = "checkRdsJob";

    private final Logger log = LoggerFactory.getLogger(FetchRdsJob.class);

    private final AccountIdSupplier allAccountIds;

    private final ClientProvider clientProvider;

    private final JobsProperties jobsProperties;

    private final ViolationSink violationSink;
    private final JobExceptionHandler jobExceptionHandler;

    @Autowired
    public FetchRdsJob(final AccountIdSupplier allAccountIds, final ClientProvider clientProvider,
                       final JobsProperties jobsProperties,
                       final ViolationSink violationSink,
                       final JobExceptionHandler jobExceptionHandler) {
        this.allAccountIds = allAccountIds;
        this.clientProvider = clientProvider;
        this.jobsProperties = jobsProperties;
        this.violationSink = violationSink;
        this.jobExceptionHandler = jobExceptionHandler;
    }

    @PostConstruct
    public void init() {
        log.info("{} initialized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 300_000)
    public void run() {
        for (final String accountId : allAccountIds.get()) {
            for (final String region : jobsProperties.getWhitelistedRegions()) {
                try {
                    final AmazonRDSClient amazonRDSClient = clientProvider.getClient(AmazonRDSClient.class, accountId,
                            Region.getRegion(Regions.fromName(region)));

                    Optional<String> marker = Optional.empty();

                    do {
                        final DescribeDBInstancesRequest request = new DescribeDBInstancesRequest();
                        marker.ifPresent(request::setMarker);
                        final DescribeDBInstancesResult result = amazonRDSClient.describeDBInstances(request);
                        marker = Optional.ofNullable(trimToNull(result.getMarker()));

                        result.getDBInstances().stream()
                                .filter(DBInstance::getPubliclyAccessible)
                                .filter(dbInstance -> dbInstance.getEndpoint() != null)
                                .forEach(dbInstance -> {
                                    final Map<String, Object> metadata = newHashMap();
                                    metadata.put("unsecuredDatabase", dbInstance.getEndpoint().getAddress());
                                    metadata.put("errorMessages", "Unsecured Database! Your DB can be reached from outside");
                                    writeViolation(accountId, region, metadata, dbInstance.getEndpoint().getAddress());

                                });

                    } while (marker.isPresent());

                } catch (final Exception e) {
                    jobExceptionHandler.onException(e, ImmutableMap.of(
                            "job", this.getClass().getSimpleName(),
                            "aws_account_id", accountId,
                            "aws_region", region));
                }
            }
        }
    }

    private void writeViolation(final String account, final String region, final Object metaInfo, final String rdsEndpoint) {
        final ViolationBuilder violationBuilder = new ViolationBuilder();
        final Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(FetchRdsJob.class)
                .withType(UNSECURED_PUBLIC_ENDPOINT)
                .withMetaInfo(metaInfo)
                .withEventId(EVENT_ID)
                .withInstanceId(rdsEndpoint)
                .build();
        violationSink.put(violation);
    }
}
