package org.zalando.stups.fullstop.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.ec2.AmazonEC2Client;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static com.amazonaws.regions.Regions.EU_CENTRAL_1;
import static com.amazonaws.regions.Regions.EU_WEST_1;
import static com.amazonaws.regions.Regions.EU_WEST_2;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration
public class CachingClientProviderTest {

    private static final String ACCOUNT_ID1 = "000000000000";

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    private static final String ACCOUNT_ID2 = "111111111111";
    private static final Region REGION1 = Region.getRegion(EU_CENTRAL_1);
    private static final Region REGION2 = Region.getRegion(EU_WEST_2);

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ClientProvider provider;

    @Test
    public void testCachingClientProvider() {
        final AmazonEC2Client client = provider.getClient(AmazonEC2Client.class, ACCOUNT_ID1, REGION1);
        assertThat(client).isNotNull();

        assertThat(provider.getClient(AmazonEC2Client.class, ACCOUNT_ID1, REGION1))
                .isNotNull()
                .isSameAs(client);
        assertThat(provider.getClient(AmazonEC2Client.class, ACCOUNT_ID2, REGION1))
                .isNotNull()
                .isNotSameAs(client);
        assertThat(provider.getClient(AmazonEC2Client.class, ACCOUNT_ID1, REGION2))
                .isNotNull()
                .isNotSameAs(client);
        assertThat(provider.getClient(AmazonCloudWatchClient.class, ACCOUNT_ID1, REGION1))
                .isNotNull()
                .isNotSameAs(client);
    }

    @Configuration
    static class TestConfig {

        @Bean
        public ClientProvider cachingClientProvider() {
            return new CachingClientProvider(EU_WEST_1.getName());
        }
    }

}
