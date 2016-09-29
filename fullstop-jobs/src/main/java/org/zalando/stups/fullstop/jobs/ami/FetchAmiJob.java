package org.zalando.stups.fullstop.jobs.ami;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.common.FetchTaupageYaml;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.taupage.TaupageYaml;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.service.ViolationService;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.zalando.stups.fullstop.violation.ViolationType.OUTDATED_TAUPAGE;

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

    private final FetchTaupageYaml fetchTaupageYaml;

    private final ViolationService violationService;
    private static final Splitter TAUPAGE_NAME_SPLITTER = Splitter.on('-');

    @Autowired
    public FetchAmiJob(final ViolationSink violationSink,
                       final ClientProvider clientProvider,
                       final AccountIdSupplier allAccountIds,
                       final JobsProperties jobsProperties,
                       final ViolationService violationService,
                       final FetchTaupageYaml fetchTaupageYaml,
                       @Value("${FULLSTOP_TAUPAGE_NAME_PREFIX}") final String taupageNamePrefix,
                       @Value("${FULLSTOP_TAUPAGE_OWNERS}") final String taupageOwners) {
        this.violationSink = violationSink;
        this.clientProvider = clientProvider;
        this.allAccountIds = allAccountIds;
        this.jobsProperties = jobsProperties;
        this.violationService = violationService;
        this.taupageNamePrefix = taupageNamePrefix;
        this.fetchTaupageYaml = fetchTaupageYaml;
        this.taupageOwners = Stream.of(taupageOwners.split(",")).filter(s -> !s.isEmpty()).collect(toList());
    }

    @PostConstruct
    public void init() {
        log.info("{} initalized", getClass().getSimpleName());
    }

    @Scheduled(fixedRate = 60_000 * 60 * 4, initialDelay = -1) // ((1 min * 60) * 4) = 4 hours rate, 0 min delay
    public void run() {
        log.info("Running job {}", getClass().getSimpleName());
        for (final String account : allAccountIds.get()) {
            for (final String region : jobsProperties.getWhitelistedRegions()) {
                runOn(account, region);
            }
        }
    }

    private void runOn(final String account, final String region) {
        try {
            log.info("Scanning EC2 instances to fetch AMIs {}/{}", account, region);
            final AmazonEC2Client ec2Client = clientProvider.getClient(
                    AmazonEC2Client.class,
                    account,
                    getRegion(fromName(region)));
            Optional<String> nextToken = empty();
            do {
                final DescribeInstancesRequest request = new DescribeInstancesRequest();
                nextToken.ifPresent(request::setNextToken);

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

    private void processInstance(String account, String region, Instance instance) {
        if (violationService.violationExists(account, region, EVENT_ID, instance.getInstanceId(), OUTDATED_TAUPAGE)) {
            return;
        }

        final Optional<Image> optionalImage = getAmiFromEC2Api(account, region, instance.getImageId());
        final Optional<Boolean> isTaupageAmi = optionalImage
                .filter(img -> img.getName().startsWith(taupageNamePrefix))
                .map(Image::getOwnerId)
                .map(taupageOwners::contains);

        // will not check for all non taupage ami
        // or images with taupage as name but created from another owner
        if (!isTaupageAmi.orElse(false)) {
            return;
        }

        final Image image = optionalImage.get();
        final Optional<LocalDate> optionalExpirationDate = getExpirationDate(image);
        final Optional<TaupageYaml> taupageYaml = fetchTaupageYaml.getTaupageYaml(instance.getInstanceId(), account, region);
        if (optionalExpirationDate.isPresent()) {
            final LocalDate expirationDate = optionalExpirationDate.get();
            if (now().isAfter(expirationDate)) {
                violationSink.put(new ViolationBuilder()
                        .withAccountId(account)
                        .withRegion(region)
                        .withPluginFullyQualifiedClassName(FetchAmiJob.class)
                        .withEventId(EVENT_ID)
                        .withType(OUTDATED_TAUPAGE)
                        .withInstanceId(instance.getInstanceId())
                        .withApplicationId(taupageYaml.map(TaupageYaml::getApplicationId).map(StringUtils::trimToNull).orElse(null))
                        .withApplicationVersion(taupageYaml.map(TaupageYaml::getApplicationVersion).map(StringUtils::trimToNull).orElse(null))
                        .withMetaInfo(ImmutableMap.of(
                                "ami_owner_id", image.getOwnerId(),
                                "ami_id", image.getImageId(),
                                "ami_name", image.getName(),
                                "expiration_date", expirationDate.toString()))
                        .build());
            }
        } else {
            log.warn("Could not expiration date of taupage AMI {}", image);
        }
    }

    private Optional<LocalDate> getExpirationDate(final Image image) {
        // current implementation parse creation date from name + add 60 days support period
        return Optional.ofNullable(image.getName())
                .filter(name -> !name.isEmpty())
                .map(TAUPAGE_NAME_SPLITTER::splitToList)
                .filter(list -> list.size() == 4) // "Taupage-AMI-20160201-123456"
                .map(parts -> parts.get(2))
                .map(timestamp -> LocalDate.parse(timestamp, ofPattern("yyyyMMdd")))
                .map(creationDate -> creationDate.plusDays(60));
    }

    private Optional<Image> getAmiFromEC2Api(final String account, final String region, final String imageId) {
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
