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
