package org.zalando.stups.fullstop.teams;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
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

public class RestTemplateTeamOperationsTest {

    private MockRestServiceServer mockServer;
    private TeamServiceProperties properties;
    private RestTemplateTeamOperations teamOperations;

    @Before
    public void setUp() throws Exception {
        final RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        properties = new TeamServiceProperties();
        teamOperations = new RestTemplateTeamOperations(restTemplate, "http://test.local", properties);
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

}
