package org.zalando.stups.fullstop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.spring.KioClientResponseErrorHandler;
import org.zalando.stups.clients.kio.spring.RestTemplateKioOperations;
import org.zalando.stups.fullstop.teams.RestTemplateTeamOperations;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.fullstop.teams.TeamServiceProperties;
import org.zalando.stups.oauth2.spring.client.StupsOAuth2RestTemplate;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.tokens.AccessTokens;

@Configuration
@EnableConfigurationProperties(TeamServiceProperties.class)
public class ClientConfig {

    private final AccessTokens accessTokens;

    private final TeamServiceProperties teamServiceProperties;

    private final String kioBaseUrl;

    private final String teamServiceBaseUrl;


    @Autowired
    public ClientConfig(
            AccessTokens accessTokens,
            TeamServiceProperties teamServiceProperties,
            @Value("${fullstop.clients.kio.url}") String kioBaseUrl,
            @Value("${fullstop.clients.teamService.url}") String teamServiceBaseUrl) {
        this.accessTokens = accessTokens;
        this.teamServiceProperties = teamServiceProperties;
        this.kioBaseUrl = kioBaseUrl;
        this.teamServiceBaseUrl = teamServiceBaseUrl;
    }

    @Bean
    KioOperations kioOperations() {
        final StupsOAuth2RestTemplate restTemplate = new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider("kio", accessTokens));
        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(4 * 1000);
        requestFactory.setReadTimeout(4 * 1000);
        restTemplate.setRequestFactory(requestFactory);
        restTemplate.setErrorHandler(new KioClientResponseErrorHandler());
        return new RestTemplateKioOperations(
                restTemplate,
                kioBaseUrl);
    }

    @Bean
    public TeamOperations teamOperations() {
        return new RestTemplateTeamOperations(
                new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider("teamService", accessTokens)),
                teamServiceBaseUrl, teamServiceProperties);
    }


}
