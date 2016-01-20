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
