package org.zalando.stups.fullstop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.spring.KioClientResponseErrorHandler;
import org.zalando.stups.clients.kio.spring.RestTemplateKioOperations;
import org.zalando.stups.fullstop.hystrix.HystrixKioOperations;
import org.zalando.stups.fullstop.hystrix.HystrixTeamOperations;
import org.zalando.stups.fullstop.teams.RestTemplateTeamOperations;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.teams.TeamServiceProperties;
import org.zalando.stups.oauth2.spring.client.StupsOAuth2RestTemplate;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.pierone.client.HystrixSpringPieroneOperations;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.pierone.client.RestTemplatePieroneOperations;
import org.zalando.stups.tokens.AccessTokens;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Configuration
@EnableConfigurationProperties(TeamServiceProperties.class)
public class ClientConfig {

    private final AccessTokens accessTokens;

    private final TeamServiceProperties teamServiceProperties;

    private final String kioBaseUrl;

    private final String teamServiceBaseUrl;

    private final String pieroneUrls;

    @Autowired
    public ClientConfig(
            AccessTokens accessTokens,
            TeamServiceProperties teamServiceProperties,
            @Value("${fullstop.clients.kio.url}") String kioBaseUrl,
            @Value("${fullstop.clients.teamService.url}") String teamServiceBaseUrl,
            @Value("${fullstop.clients.pierone.urls}") String pieroneUrls) {
        this.accessTokens = accessTokens;
        this.teamServiceProperties = teamServiceProperties;
        this.kioBaseUrl = kioBaseUrl;
        this.teamServiceBaseUrl = teamServiceBaseUrl;
        this.pieroneUrls = pieroneUrls;
    }

    @Bean
    public KioOperations kioOperations() {
        return new HystrixKioOperations(
                new RestTemplateKioOperations(
                        buildOAuth2RestTemplate("kio", new KioClientResponseErrorHandler()),
                        kioBaseUrl));
    }

    @Bean
    public TeamOperations teamOperations() {
        return new HystrixTeamOperations(
                new RestTemplateTeamOperations(
                        buildOAuth2RestTemplate("teamService"),
                        teamServiceBaseUrl, teamServiceProperties));
    }

    @Bean
    public Function<String, PieroneOperations> pieroneOperationsProvider() {
        return Stream.of(pieroneUrls.split(","))
                .map(ClientConfig::toUrl)
                .collect(toMap(URL::getHost, this::newPieroneOperations))
                ::get;
    }

    private static URL toUrl(final String urlString) {
        try {
            return new URL(urlString);
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private PieroneOperations newPieroneOperations(final URL baseUrl) {
        return new HystrixSpringPieroneOperations(
                new RestTemplatePieroneOperations(
                        buildOAuth2RestTemplate("pierone"),
                        baseUrl.toString()));
    }

    private RestOperations buildOAuth2RestTemplate(final String tokenName) {
        return buildOAuth2RestTemplate(tokenName, null);
    }

    private RestOperations buildOAuth2RestTemplate(final String tokenName, final ResponseErrorHandler errorHandler) {
        final RestTemplate restTemplate = new StupsOAuth2RestTemplate(
                new StupsTokensAccessTokenProvider(tokenName, accessTokens),
                new HttpComponentsClientHttpRequestFactory());

        if (errorHandler != null) {
            restTemplate.setErrorHandler(errorHandler);
        }

        return restTemplate;
    }

}
