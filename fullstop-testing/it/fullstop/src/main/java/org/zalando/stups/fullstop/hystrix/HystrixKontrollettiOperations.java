package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.web.client.HttpClientErrorException;
import org.zalando.kontrolletti.KontrollettiOperations;

public class HystrixKontrollettiOperations implements KontrollettiOperations {

    private final KontrollettiOperations delegate;

    public HystrixKontrollettiOperations(KontrollettiOperations delegate) {
        this.delegate = delegate;
    }

    @Override
    @HystrixCommand(ignoreExceptions = HttpClientErrorException.class)
    public String normalizeRepositoryUrl(String repositoryUrl) {
        return delegate.normalizeRepositoryUrl(repositoryUrl);
    }
}
