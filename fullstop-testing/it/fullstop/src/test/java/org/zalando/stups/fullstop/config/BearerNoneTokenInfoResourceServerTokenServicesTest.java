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

import org.junit.Before;
import org.junit.Test;

import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;

import org.zalando.stups.oauth2.spring.server.TokenInfoResourceServerTokenServices;

/**
 * Tests for {@link BearerNoneTokenInfoResourceServerTokenServices}.
 *
 * @author  jbellmann
 */
public class BearerNoneTokenInfoResourceServerTokenServicesTest {

    private TokenInfoResourceServerTokenServices tir;

    @Before
    public void setUp() {
        tir = new BearerNoneTokenInfoResourceServerTokenServices("http://host/path");
    }

    @Test(expected = InvalidTokenException.class)
    public void nullAccessToken() {
        tir.loadAuthentication(null);
    }

    @Test(expected = InvalidTokenException.class)
    public void emptyAccessToken() {
        tir.loadAuthentication("");
    }

    @Test(expected = InvalidTokenException.class)
    public void whitspaceAccessToken() {
        tir.loadAuthentication("    ");
    }

    @Test(expected = InvalidTokenException.class)
    public void noneAccessToken() {
        tir.loadAuthentication("None");
    }

    @Test(expected = InvalidTokenException.class)
    public void noneIgnoreCaseAccessTokenAllUppercase() {
        tir.loadAuthentication("NONE");
    }

    @Test(expected = InvalidTokenException.class)
    public void noneIgnoreCaseAccessTokenAllLowercase() {
        tir.loadAuthentication("none");
    }

    @Test(expected = InvalidTokenException.class)
    public void less_than_30_AccessToken() {
        tir.loadAuthentication("12345678909876543211234567898");
    }
}
