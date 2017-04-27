package org.zalando.stups.differentnamespace;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SimpleBootApplication.class, properties = "debug=true")
@ActiveProfiles("single")
public class SimpleBootApplicationSingleIT {

    @Test
    public void dontCare() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }
}
