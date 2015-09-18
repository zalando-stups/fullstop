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

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import org.springframework.util.StringUtils;

import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

/**
 * Validates the incoming 'accessToken' before delegates to 'super'.<br/>
 * TODO move this to base-library.
 *
 * @author  jbellmann
 */
class BearerNoneTokenInfoResourceServerTokenServices extends TokenInfoResourceServerTokenServices {

    private static final String NONE = "None";

    public BearerNoneTokenInfoResourceServerTokenServices(final String tokenInfoEndpointUrl) {
        super(tokenInfoEndpointUrl);
    }

    @Override
    public OAuth2Authentication loadAuthentication(final String accessToken) throws AuthenticationException,
        InvalidTokenException {
        if (!StringUtils.hasText(accessToken)) {
            throw new InvalidTokenException("AccessToken should not be 'null', 'empty' or 'whitespace'");
        }

        if (NONE.equalsIgnoreCase(accessToken)) {
            throw new InvalidTokenException("AccessToken should not be 'None'");
        }

        if (accessToken.length() < 30) {
            throw new InvalidTokenException("AccessToken should have a length of 30 at least ");
        }

        return super.loadAuthentication(accessToken);
    }

}
