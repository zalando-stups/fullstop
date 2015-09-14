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
    public TeamOperations teamOperations() {
        return new RestTemplateTeamOperations(
                new StupsOAuth2RestTemplate(new StupsTokensAccessTokenProvider("teamService", accessTokens)),
                teamServiceBaseUrl);
    }
}
