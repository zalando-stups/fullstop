package org.zalando.stups.fullstop.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.teams.TeamOperations;
import org.zalando.stups.pierone.client.PieroneOperations;
import org.zalando.stups.tokens.AccessTokens;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ClientConfigTest {

    @Autowired(required = false)
    private KioOperations kioOperations;

    @Autowired(required = false)
    private Function<String, PieroneOperations> pieroneOperationsProvider;

    @Autowired(required = false)
    private TeamOperations teamOperations;

    @Autowired(required = false)
    private KontrollettiOperations kontrollettiOperations;

    @Test
    public void testKioOperations() throws Exception {
        assertThat(kioOperations).isNotNull();
    }

    @Test
    public void testPieroneOperations() throws Exception {
        assertThat(pieroneOperationsProvider).isNotNull();
        assertThat(pieroneOperationsProvider.apply("pierone.local")).isNotNull();
        assertThat(pieroneOperationsProvider.apply("opensource.local")).isNotNull();
        assertThat(pieroneOperationsProvider.apply("unknown.registry")).isNull();
    }

    @Test
    public void testTeamOperations() throws Exception {
        assertThat(teamOperations).isNotNull();
    }

    @Test
    public void testKontrollettiOperations() throws Exception {
        assertThat(kontrollettiOperations).isNotNull();
    }

    @Configuration
    @Import(ClientConfig.class)
    @PropertySource("classpath:config/application-ClientConfigTest.properties")
    static class TestConfig {

        @Bean static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }

        @Bean AccessTokens accessTokens() {
            return mock(AccessTokens.class);
        }
    }
}
