package org.zalando.stups.fullstop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.kontrolletti.HystrixKontrollettiOperations;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.kontrolletti.RestTemplateKontrollettiOperations;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.spring.KioClientResponseErrorHandler;
import org.zalando.stups.clients.kio.spring.RestTemplateKioOperations;
import org.zalando.stups.fullstop.teams.RestTemplateTeamOperations;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.oauth2.spring.client.StupsOAuth2RestTemplate;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.tokens.AccessTokens;

@Configuration
public class ClientConfig {

    @Autowired
    private AccessTokens accessTokens;

    @Value("${fullstop.clients.kio.url}")
    private String kioBaseUrl;

    @Value("${fullstop.clients.kontrolletti.url}")
    private String kontrollettiBaseUrl;

    @Value("${fullstop.clients.teamService.url}")
    private String teamServiceBaseUrl;

    @Bean
    KioOperations kioOperations() {
        final StupsOAuth2RestTemplate restTemplate = new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider("kio", accessTokens));
        restTemplate.setErrorHandler(new KioClientResponseErrorHandler());
        return new RestTemplateKioOperations(
                restTemplate,
                kioBaseUrl);
    }

    @Bean
    public KontrollettiOperations kontrollettiOperations() {
        return new HystrixKontrollettiOperations(
                new RestTemplateKontrollettiOperations(
                        new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider("kontrolletti", accessTokens)),
                        kontrollettiBaseUrl));
    }

    @Bean
    public TeamOperations teamOperations() {
        return new RestTemplateTeamOperations(
                new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider("teamService", accessTokens)),
                teamServiceBaseUrl);
    }


}
