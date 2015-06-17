/**
 * Copyright 2015 Zalando SE
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
package org.zalando.stups.fullstop.clients.pierone.spring;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RestTemplatePieroneOperationsTest {

    private final String baseUrl = "http://localhost:8080";

    private OAuth2RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private RestTemplatePieroneOperations client;

    @Before
    public void setUp() {

        BaseOAuth2ProtectedResourceDetails resource = new BaseOAuth2ProtectedResourceDetails();
        resource.setClientId("what_here");

        restTemplate = new OAuth2RestTemplate(resource);
        restTemplate.setAccessTokenProvider(new TestAccessTokenProvider("86c45354-8bc4-44bf-905f-5f34ebe0b599"));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        client = new RestTemplatePieroneOperations(restTemplate, baseUrl);

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    //J-
    @Test
    public void getRefoles() {
        mockServer.expect(requestTo(baseUrl + "/v1/repositories/testTeam/testApplication/tags"))
                .andExpect(method(GET))
                .andRespond(withSuccess(ResourceUtil.resource("/getTags"), APPLICATION_JSON));

        Map<String, String> resultMap = client.listTags("testTeam", "testApplication");
        assertThat(resultMap).isNotNull();
        assertThat(resultMap).isNotEmpty();
        assertThat(resultMap.size()).isEqualTo(6);
        assertThat(resultMap.get("0.7")).isEqualTo("febd155ed19b741f2c87f162ed6183c84039b7c8e43eb0136d475f816a821894");

        mockServer.verify();
    }
    //J+
}
