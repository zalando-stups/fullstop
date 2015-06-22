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
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.web.client.RestOperations;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.spring.RestTemplateKioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.clients.pierone.spring.RestTemplatePieroneOperations;
import org.zalando.stups.fullstop.teams.RestTemplateTeamOperations;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.oauth2.spring.client.AutoRefreshTokenProvider;
import org.zalando.stups.oauth2.spring.client.StupsAccessTokenProvider;
import org.zalando.stups.tokens.AccessTokens;

/**
 * @author  jbellmann
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

    @Bean
    public KioOperations kioOperations() {
        return new RestTemplateKioOperations(buildOAuth2RestTemplate("kio"), kioBaseUrl);
    }

    @Bean
    public PieroneOperations pieroneOperations() {
        return new RestTemplatePieroneOperations(buildOAuth2RestTemplate("pierone"), pieroneBaseUrl);
    }

    @Bean
    public TeamOperations teamOperations() {
        return new RestTemplateTeamOperations(buildOAuth2RestTemplate("teamService"), teamServiceBaseUrl);
    }

    private RestOperations buildOAuth2RestTemplate(String tokenName) {
        final BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("fullstop");

        final OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource);
        restTemplate.setAccessTokenProvider(new StupsAccessTokenProvider(
                new AutoRefreshTokenProvider(tokenName, accessTokens)));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }

}
