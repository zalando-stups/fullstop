package org.zalando.stups.fullstop.jobs.scm;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.kontrolletti.ListCommitsResponse;
import org.zalando.kontrolletti.resources.Repository;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.ApplicationBase;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.jobs.FullstopJob;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.entity.AccountRegion;
import org.zalando.stups.fullstop.violation.service.ApplicationLifecycleService;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.kontrolletti.CommitRangeRequest.Builder.inRepository;
import static org.zalando.stups.fullstop.violation.ViolationType.MISSING_SPEC_LINKS;

@Component
public class ScmCommitsJob implements FullstopJob {

    private final Logger log = getLogger(getClass());

    private static final String EVENT_ID = "scmCommitsJob";
    private final KioOperations kio;
    private final KontrollettiOperations kontrolletti;
    private final ApplicationLifecycleService lifecycle;
    private final ViolationSink violationSink;

    @Autowired
    public ScmCommitsJob(
            KioOperations kio,
            KontrollettiOperations kontrolletti,
            ApplicationLifecycleService lifecycle,
            ViolationSink violationSink) {
        this.kio = kio;
        this.kontrolletti = kontrolletti;
        this.lifecycle = lifecycle;
        this.violationSink = violationSink;
    }

    @Override
    public void run() {
        kio.listApplications().stream()
                .map(ApplicationBase::getId)
                .map(kio::getApplicationById)
                .filter(Application::isActive)
                .forEach(this::processApplication);
    }

    private void processApplication(Application app) {
        try {
            lifecycle.findDeployments(app.getId()).forEach(deployment ->
                    Optional.of(app.getScmUrl())
                            .filter(StringUtils::isNotBlank)
                            .map(kontrolletti::normalizeRepositoryUrl) // TODO is this necessary?
                            .map(kontrolletti::getRepository)
                            .flatMap(repo -> findViolationInRepo(repo, deployment, app))
                            .ifPresent(violationSink::put));
        } catch (Exception e) {
            log.warn("Failed to verify commits in scm repository of " + app, e);
        }
    }

    private Optional<Violation> findViolationInRepo(Repository repository, AccountRegion deployment, Application app) {
        final ZonedDateTime yesterdayMidnight = now().minusDays(1).atStartOfDay(UTC);
        final ZonedDateTime todayMidnight = yesterdayMidnight.plusDays(1);
        return Optional.ofNullable(kontrolletti.listCommits(
                inRepository(repository)
                        .fromDate(yesterdayMidnight)
                        .toDate(todayMidnight)
                        .isValid(false)
                        .perPage(10)
                        .build()))
                .map(ListCommitsResponse::getContent)
                .filter(list -> !list.isEmpty())
                .map(invalidCommits -> new ViolationBuilder()
                        .withAccountId(deployment.getAccount())
                        .withRegion(deployment.getRegion())
                        .withType(MISSING_SPEC_LINKS)
                        .withEventId(EVENT_ID)
                        .withPluginFullyQualifiedClassName(ScmCommitsJob.class)
                        .withMetaInfo(ImmutableMap.of(
                                "application_id", app.getId(),
                                "repository", repository.getUrl(),
                                "invalid_commits", invalidCommits.stream()
                                        .map(commit -> commit.getCommitId() + " (" + abbreviate(commit.getMessage(), 32) + ")")
                                        .collect(joining("; "))))
                        .build());

    }

}
