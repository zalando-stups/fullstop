package org.zalando.stups.fullstop.aws;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author jbellmann
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CachingClientProviderTest {

    @Autowired
    private ClientProvider provider;

    @Test
    public void testCachingClientProvider() throws InterruptedException {
        AmazonWebServiceClient client = provider.getClient(
                AmazonEC2Client.class, "",
                Region.getRegion(Regions.EU_CENTRAL_1));

        Assertions.assertThat(client).isNotNull();
        System.out.println(client.toString());
        for (int i = 0; i < 10; i++) {

            AmazonEC2Client other = provider.getClient(
                    AmazonEC2Client.class, "",
                    Region.getRegion(Regions.EU_CENTRAL_1));

            Assertions.assertThat(other).isNotNull();
            Assertions.assertThat(other).isEqualTo(client);
            System.out.println(other.toString());
            TimeUnit.SECONDS.sleep(2);
        }

    }

    @Configuration
    static class TestConfig {

        @Bean
        public ClientProvider cachingClientProvider() {
            return new CachingClientProvider();
        }
    }

}
