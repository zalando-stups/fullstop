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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;

/**
 * @author jbellmann
 */
@Configuration
@EnableResourceServer
public class OAuth2Configuration extends ResourceServerConfigurerAdapter {

    @Value("${spring.oauth2.resource.tokenInfoUri}")
    private String tokenInfoUri;

    /**
     * Configure scopes for specific controller/httpmethods/roles here.
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception {

        http
                .sessionManagement()
                .sessionCreationPolicy(NEVER)

                // configure form login
                .and().formLogin().disable()

                // configure logout
                .logout().disable()

                .authorizeRequests()

                    .antMatchers("/").permitAll()
                    .antMatchers("/webjars/**").permitAll()
                    .antMatchers("/swagger-resources").permitAll()
                    .antMatchers("/api-docs").permitAll()
                    .antMatchers("/health").permitAll()

                // You MUST use Pre/Post authorize
                .antMatchers("/api").denyAll()
//                .antMatchers(GET, "/api/**").access("#oauth2.hasScope('uid')")
//                .antMatchers(POST, "/api/**").access("#oauth2.hasScope('uid')")
                .antMatchers(GET, "/s3/**").access("#oauth2.hasScope('uid')");


    }

    @Bean
    public ResourceServerTokenServices customResourceTokenServices() {
        return new TokenInfoResourceServerTokenServices(tokenInfoUri, "what_here");
    }
}
