/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.spring.RestTemplateKioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.clients.pierone.spring.RestTemplatePieroneOperations;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
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

    @Bean
    public KioOperations kioOperations() {

        // maybe we can share this
        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("fullstop");

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource);

        // here is the token-provider
        restTemplate.setAccessTokenProvider(new StupsTokensAccessTokenProvider("kio", accessTokens));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        return new RestTemplateKioOperations(restTemplate, kioBaseUrl);
    }

    @Bean
    public PieroneOperations pieroneOperations() {

        // maybe we can share this
        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("fullstop");

        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource);

        // here is the token-provider
        restTemplate.setAccessTokenProvider(new StupsTokensAccessTokenProvider("pierone", accessTokens));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        return new RestTemplatePieroneOperations(restTemplate, pieroneBaseUrl);
    }
}
