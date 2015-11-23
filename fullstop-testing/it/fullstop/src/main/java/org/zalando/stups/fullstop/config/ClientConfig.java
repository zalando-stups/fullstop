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
package org.zalando.stups.fullstop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.kontrolletti.KontrollettiResponseErrorHandler;
import org.zalando.kontrolletti.RestTemplateKontrollettiOperations;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.spring.KioClientResponseErrorHandler;
import org.zalando.stups.clients.kio.spring.RestTemplateKioOperations;
import org.zalando.stups.fullstop.hystrix.HystrixKioOperations;
import org.zalando.stups.fullstop.hystrix.HystrixKontrollettiOperations;
import org.zalando.stups.fullstop.hystrix.HystrixTeamOperations;
import org.zalando.stups.fullstop.teams.RestTemplateTeamOperations;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.oauth2.spring.client.StupsOAuth2RestTemplate;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.pierone.client.HystrixSpringPieroneOperations;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.pierone.client.RestTemplatePieroneOperations;
import org.zalando.stups.tokens.AccessTokens;

/**
 * @author jbellmann
 */
@Configuration
public class ClientConfig {

    @Autowired
    private AccessTokens accessTokens;

    @Value("${fullstop.clients.kio.url}")
    private String kioBaseUrl;

    @Value("${fullstop.clients.pierone.url}")
    private String pieroneBaseUrl;

    @Value("${fullstop.clients.teamService.url}")
    private String teamServiceBaseUrl;

    @Value("${fullstop.clients.kontrolletti.url}")
    private String kontrollettiBaseUrl;

    @Bean
    public KioOperations kioOperations() {
        return new HystrixKioOperations(
                new RestTemplateKioOperations(
                        buildOAuth2RestTemplate("kio", new KioClientResponseErrorHandler()),
                        kioBaseUrl));
    }

    @Bean
    public PieroneOperations pieroneOperations() {
        return new HystrixSpringPieroneOperations(
                new RestTemplatePieroneOperations(
                        buildOAuth2RestTemplate("pierone"),
                        pieroneBaseUrl));
    }

    @Bean
    public TeamOperations teamOperations() {
        return new HystrixTeamOperations(
                new RestTemplateTeamOperations(
                        buildOAuth2RestTemplate("teamService"),
                        teamServiceBaseUrl));
    }

    @Bean
    public KontrollettiOperations kontrollettiOperations() {
        return new HystrixKontrollettiOperations(
                new RestTemplateKontrollettiOperations(
                        buildOAuth2RestTemplate("kontrolletti", new KontrollettiResponseErrorHandler()),
                        kontrollettiBaseUrl));
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
