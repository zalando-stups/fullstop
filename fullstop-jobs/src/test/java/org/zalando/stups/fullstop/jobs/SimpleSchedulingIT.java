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
package org.zalando.stups.fullstop.jobs;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * Can we build custom-annotations from existing?
 *
 * @author jbellmann
 */
// @Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SimpleSchedulingIT {

    @Autowired
    private SimpleScheduledBean simpleScheduledBean;

    @Test
    public void run() throws InterruptedException {
        System.out.println("START");
        TimeUnit.SECONDS.sleep(22);
        System.out.println("END");
        Assertions.assertThat(simpleScheduledBean.getInvocationCount()).isGreaterThan(1);

    }

    @Configuration
    @EnableScheduling
    static class TestConfig {

        @Bean
        public SimpleScheduledBean simpleScheduledBean() {
            return new SimpleScheduledBean();
        }
    }
}
