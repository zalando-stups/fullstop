package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.web.client.HttpClientErrorException;
import org.zalando.stups.clients.kio.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class HystrixKioOperations implements KioOperations {

    private final KioOperations delegate;

    public HystrixKioOperations(final KioOperations delegate) {
        this.delegate = delegate;
    }

    @Override
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class})
    public List<ApplicationBase> listApplications() {
        return delegate.listApplications();
    }

    @Override
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class, IllegalArgumentException.class})
    public List<ApplicationBase> listApplications(final Optional<ZonedDateTime> modifiedBefore, final Optional<ZonedDateTime> modifiedAfter) {
        return delegate.listApplications(modifiedBefore, modifiedAfter);
    }

    @Override
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class, IllegalArgumentException.class})
    public List<ApplicationSearchResult> searchApplications(final String query, final Optional<ZonedDateTime> modifiedBefore, final Optional<ZonedDateTime> modifiedAfter) {
        return delegate.searchApplications(query, modifiedBefore, modifiedAfter);
    }

    @Override
    @HystrixCommand(ignoreExceptions = {NotFoundException.class, HttpClientErrorException.class, IllegalArgumentException.class})
    public Application getApplicationById(final String applicationId) {
        return delegate.getApplicationById(applicationId);
    }

    @Override
    @HystrixCommand(ignoreExceptions = {HttpClientErrorException.class, IllegalArgumentException.class})
    public void createOrUpdateApplication(final CreateOrUpdateApplicationRequest request, final String applicationId) {
        delegate.createOrUpdateApplication(request, applicationId);
    }
}
