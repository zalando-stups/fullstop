package org.zalando.stups.fullstop.aws;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;

import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.AmazonWebServiceClient;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.ec2.AmazonEC2Client;

/**
 * @author  jbellmann
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CachingClientProviderTest {

    @Autowired
    private CachingClientProvider provider;

    @Test
    public void testCachingClientProvider() throws InterruptedException {
        AmazonWebServiceClient client = provider.getClient(AmazonEC2Client.class, "786011980701",
                Region.getRegion(Regions.EU_CENTRAL_1));

        Assertions.assertThat(client).isNotNull();
        System.out.println(client.toString());
        for (int i = 0; i < 10; i++) {

            AmazonEC2Client other = provider.getClient(AmazonEC2Client.class, "786011980701",
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
        public CachingClientProvider cachingClientProvider() {
            return new CachingClientProviderImpl();
        }
    }

}
