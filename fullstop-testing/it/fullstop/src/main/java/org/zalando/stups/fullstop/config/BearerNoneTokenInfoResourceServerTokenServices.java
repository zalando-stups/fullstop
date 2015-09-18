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
