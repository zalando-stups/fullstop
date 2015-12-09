package org.zalando.stups.differentnamespace;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SimpleBootApplication.class)
@IntegrationTest("debug=true")
@ActiveProfiles("triple")
public class SimpleBootApplicationTripleIT {

    @Test
    public void dontCare() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }
}
