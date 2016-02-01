package org.zalando.stups.fullstop.jobs.ami;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.violation.ViolationType.OUTDATED_AMI;

@Component
public class FetchAmiJob implements FullstopJob {

    private static final String EVENT_ID = "checkAmiJob";

    private final String taupageNamePrefix;

    private final List<String> taupageOwners;

    private final Logger log = LoggerFactory.getLogger(FetchAmiJob.class);

    private final ViolationSink violationSink;

    private final ClientProvider clientProvider;

    private final AccountIdSupplier allAccountIds;

    private final JobsProperties jobsProperties;

    private final ViolationService violationService;

    @Autowired
    public FetchAmiJob(ViolationSink violationSink,
                       ClientProvider clientProvider,
                       AccountIdSupplier allAccountIds, JobsProperties jobsProperties,
                       ViolationService violationService,
                       @Value("${FULLSTOP_TAUPAGE_NAME_PREFIX}") final String taupageNamePrefix,
                       @Value("${FULLSTOP_TAUPAGE_OWNERS}") final String taupageOwners) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
        this.jobsProperties = jobsProperties;
        this.violationService = violationService;
        this.taupageNamePrefix = taupageNamePrefix;
        this.taupageOwners = Stream.of(taupageOwners.split(",")).filter(s -> !s.isEmpty()).collect(toList());
    }

    @PostConstruct
    public void init() {
        log.info("{} initalized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 60_000 * 60 * 4, initialDelay = -1) // ((1 min * 60) * 4) = 4 hours rate, 0 min delay
    public void run() {
        log.info("Running job {}", getClass().getSimpleName());
        for (String account : allAccountIds.get()) {
            for (String region : jobsProperties.getWhitelistedRegions()) {

                try {

                    log.info("Scanning EC2 instances to fetch AMIs {}/{}", account, region);

                    DescribeInstancesResult describeEC2Result = getDescribeEC2Result(
                            account,
                            region);

                    for (final Reservation reservation : describeEC2Result.getReservations()) {

                        for (final Instance instance : reservation.getInstances()) {
                            final Map<String, Object> metaData = newHashMap();
                            final List<String> errorMessages = newArrayList();

                            if (violationService.violationExists(account, region, EVENT_ID, instance.getInstanceId(), OUTDATED_AMI)) {
                                continue;
                            }

                            final Optional<Image> image = getAmiFromEC2Api(account, region, instance.getImageId());
                            final Optional<Boolean> isTaupageAmi = image
                                    .filter(img -> img.getName().startsWith(taupageNamePrefix))
                                    .map(Image::getOwnerId)
                                    .map(taupageOwners::contains);

                            // will not check for all non taupage ami
                            // or images with taupage as name but created from another owner
                            if (!isTaupageAmi.orElse(false)) {
                                continue;
                            }

                            DateTime now = DateTime.now();
                            if (isTaupageTooOld(image.map(Image::getName), now)) {
                                metaData.put("ami_owner_id", image.map(Image::getOwnerId).orElse(""));
                                metaData.put("ami_id", image.map(Image::getImageId).orElse(""));
                                metaData.put("ami_name", image.map(Image::getName).orElse(""));
                                metaData.put("taupage_image_date", getTaupageImageDate(image.map(Image::getName)));
                                metaData.put("expiration_date", getTaupageImageDate(image.map(Image::getName)).plus(Days.days(60)));
                            }

                            if (metaData.size() > 0) {
                                metaData.put("errorMessages", errorMessages);
                                writeViolation(account, region, metaData, instance.getInstanceId());
                            }

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


    private DateTime getTaupageImageDate(Optional<String> imageName) {
        String rawDate = imageName.map(s -> Stream.of(s.split("-")).collect(Collectors.toList())).get().get(2);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
        return formatter.parseDateTime(rawDate);
    }

    private boolean isTaupageTooOld(Optional<String> imageName, DateTime now) {
        DateTime maxValidityTimeForAmi = now.minus(Days.days(60));
        DateTime taupageImageDate = getTaupageImageDate(imageName);
        return taupageImageDate.isBefore(maxValidityTimeForAmi);
    }

    private void writeViolation(String account, String region, Object metaInfo, String instanceId) {
        ViolationBuilder violationBuilder = new ViolationBuilder();
        Violation violation = violationBuilder.withAccountId(account)
                .withRegion(region)
                .withPluginFullyQualifiedClassName(FetchAmiJob.class)
                .withType(OUTDATED_AMI)
                .withMetaInfo(metaInfo)
                .withInstanceId(instanceId)
                .withEventId(EVENT_ID).build();
        violationSink.put(violation);
    }

    private DescribeInstancesResult getDescribeEC2Result(String account, String region) {
        final AmazonEC2Client ec2Client = clientProvider.getClient(
                AmazonEC2Client.class,
                account,
                getRegion(fromName(region)));
        return ec2Client.describeInstances(new DescribeInstancesRequest());
    }

    private Optional<Image> getAmiFromEC2Api(String account, String region, final String imageId) {
        try {
            final AmazonEC2Client ec2Client = clientProvider.getClient(
                    AmazonEC2Client.class,
                    account,
                    getRegion(fromName(region)));

            final DescribeImagesResult response = ec2Client.describeImages(new DescribeImagesRequest().withImageIds(imageId));

            return ofNullable(response)
                    .map(DescribeImagesResult::getImages)
                    .map(List::stream)
                    .flatMap(Stream::findFirst);

        } catch (final AmazonClientException e) {
            log.warn("Could not describe image " + imageId, e);
            return empty();
        }
    }
}
