package org.zalando.stups.fullstop.config;

import static org.joda.time.DateTime.now;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.io.IOException;

import org.junit.Test;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResponseErrorHandler;
import org.zalando.stups.oauth2.spring.client.StupsOAuth2RestTemplate;
import org.zalando.stups.oauth2.spring.client.StupsTokensAccessTokenProvider;
import org.zalando.stups.tokens.AccessToken;
import org.zalando.stups.tokens.AccessTokens;

public class OAuth2RestTemplateIntegrationTest {

    @Test
    public void testOAuth2RestTemplate() throws Exception {
        final AccessTokens mockAccessTokens = mock(AccessTokens.class);
        when(mockAccessTokens.get(anyObject()))
                .thenReturn("token-0815")
                .thenReturn("token-4711");
        when(mockAccessTokens.getAccessToken(anyObject()))
                // first token is already expired
                .thenReturn(new AccessToken("token-0815", "bearer", 3600, now().minusHours(1).toDate()))
                        // second token is valid
                .thenReturn(new AccessToken("token-4711", "bearer", 3600, now().plusHours(1).toDate()));

        final StupsOAuth2RestTemplate restTemplate = new StupsOAuth2RestTemplate(
                new StupsTokensAccessTokenProvider("unit-test", mockAccessTokens));
        restTemplate.setErrorHandler(new PassThroughErrorHandler());

        final MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

        // first request with invalid token
        mockServer.expect(requestTo("/first"))
                  .andExpect(method(GET))
                  .andExpect(header("Authorization", "Bearer token-0815"))
                  .andRespond(withUnauthorizedRequest());

        // second request with valid token
        mockServer.expect(requestTo("/second"))
                  .andExpect(method(GET))
                  .andExpect(header("Authorization", "Bearer token-4711"))
                  .andRespond(withUnauthorizedRequest());

        // perform first request
        restTemplate.getForEntity("/first", String.class);
        // perform second request
        restTemplate.getForEntity("/second", String.class);

        mockServer.verify();
    }

    private static class PassThroughErrorHandler implements ResponseErrorHandler {
        @Override public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
            return false;
        }

        @Override public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

        }
    }
}
