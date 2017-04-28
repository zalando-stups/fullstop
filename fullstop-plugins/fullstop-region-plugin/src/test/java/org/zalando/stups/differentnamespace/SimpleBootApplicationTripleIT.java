package org.zalando.stups.differentnamespace;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleBootApplication.class, properties = "debug=true")
@ActiveProfiles("triple")
public class SimpleBootApplicationTripleIT {

    @Test
    public void dontCare() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }
}
