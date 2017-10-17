package org.zalando.stups.fullstop.teams;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.main.banner-mode=off")
public class RestTemplateTeamOperationsTest {

    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private TeamServiceProperties properties;
    @Autowired
    private RestTemplateTeamOperations teamOperations;

    @Before
    public void setUp() throws Exception {
        mockServer.reset();
    }

    @Test
    public void getAwsAccountsByUser() throws Exception {
        properties.setAwsMembershipRoles("PowerUser,MasterOfDisaster");
        final String uid = "mickeymouse";

        mockAccountsRequest(uid, "PowerUser", "power-user-accounts.json");
        mockAccountsRequest(uid, "MasterOfDisaster", "master-of-disaster-accounts.json");

        final List<Account> accounts = teamOperations.getAwsAccountsByUser(uid);

        assertThat(accounts).containsOnly(
                new Account("111222333444", "firstaccount", "aws", "", "dagobert", false),
                new Account("555666777888", "secondaccount", "aws", "", "dagobert", false),
                new Account("999000111222", "thirdaccount", "aws", "", "ticktricktrack", true)
        );
        mockServer.verify();
    }

    private void mockAccountsRequest(String uid, String role, String responseBodyFile) {
        mockServer.expect(method(GET)).andExpect(requestTo(startsWith("http://test.local/api/accounts/aws?")))
                .andExpect(queryParam("member", uid)).andExpect(queryParam("role", role))
                .andRespond(withSuccess(new ClassPathResource(responseBodyFile), APPLICATION_JSON));
    }

    @Configuration
    @EnableConfigurationProperties(TeamServiceProperties.class)
    public static class TestConfig {

        @Bean
        RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        MockRestServiceServer mockServer(RestTemplate restTemplate) {
            return MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
        }

        @Bean
        RestTemplateTeamOperations teamOperations(RestTemplate restTemplate, TeamServiceProperties properties) {
            return new RestTemplateTeamOperations(restTemplate, "http://test.local", properties);
        }

    }

}
