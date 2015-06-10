/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
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
        AmazonWebServiceClient client = provider.getClient(AmazonEC2Client.class, "",
                Region.getRegion(Regions.EU_CENTRAL_1));

        Assertions.assertThat(client).isNotNull();
        System.out.println(client.toString());
        for (int i = 0; i < 10; i++) {

            AmazonEC2Client other = provider.getClient(AmazonEC2Client.class, "",
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
