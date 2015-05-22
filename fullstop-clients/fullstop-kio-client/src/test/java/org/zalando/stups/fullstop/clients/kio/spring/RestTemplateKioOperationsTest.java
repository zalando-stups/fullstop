package org.zalando.stups.fullstop.clients.kio.spring;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import static org.zalando.stups.fullstop.clients.kio.spring.ResourceUtil.resource;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;

import org.springframework.test.web.client.MockRestServiceServer;

import org.zalando.stups.fullstop.clients.kio.Application;
import org.zalando.stups.fullstop.clients.kio.ApplicationBase;
import org.zalando.stups.fullstop.clients.kio.Version;

/**
 * @author  jbellmann
 */
public class RestTemplateKioOperationsTest {

    private final String baseUrl = "http://localhost:8080";

    private OAuth2RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private RestTemplateKioOperations client;

    @Before
    public void setUp() {

        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("what_here");

        restTemplate = new OAuth2RestTemplate(resource);
        restTemplate.setAccessTokenProvider(new TestAccessTokenProvider("86c45354-8bc4-44bf-905f-5f34ebe0b599"));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        client = new RestTemplateKioOperations(restTemplate, baseUrl);

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    //J-
    @Test
    public void getRefoles() {
        mockServer.expect(requestTo(baseUrl + "/apps"))
                    .andExpect(method(GET))
                    .andRespond(withSuccess(resource("/getApplications"), APPLICATION_JSON));

        List<ApplicationBase> resultLists = client.listApplications();
        assertThat(resultLists).isNotNull();
        assertThat(resultLists).isNotEmpty();
        assertThat(resultLists.size()).isEqualTo(1);

        mockServer.verify();
    }

    @Test
    public void getApplicationById() {
        mockServer.expect(requestTo(baseUrl + "/apps/kio"))
                    .andExpect(method(GET))
                    .andRespond(withSuccess(resource("/getApplicationById"), APPLICATION_JSON));

        Application application = client.getApplicationById("kio");
        assertThat(application).isNotNull();
        assertThat(application.getId()).isEqualTo("kio");

        mockServer.verify();
    }

    @Test
    public void getApplicationVersion() {
        mockServer.expect(requestTo(baseUrl + "/apps/kio/versions/1"))
                    .andExpect(method(GET))
                    .andRespond(withSuccess(resource("/getApplicationVersion"), APPLICATION_JSON));

        Version version = client.getApplicationVersion("kio", "1");
        assertThat(version).isNotNull();
        assertThat(version.getApplicationId()).isEqualTo("kio");
        assertThat(version.getId()).isEqualTo("1");


        mockServer.verify();
    }
    //J+

}
