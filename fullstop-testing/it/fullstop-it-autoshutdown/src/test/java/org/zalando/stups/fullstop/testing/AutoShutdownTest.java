package org.zalando.stups.fullstop.testing;

import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class AutoShutdownTest {

    @Test
    public void simpleTest() throws InterruptedException {
        AbstractApplicationContext acc = mock(AbstractApplicationContext.class);
        AutoShutdown shutdown = new AutoShutdown();
        shutdown.setApplicationContext(acc);
        shutdown.timeout = 1;
        shutdown.init();

        for (int i = 0; i < 10; i++) {

            shutdown.shutdown();
            TimeUnit.SECONDS.sleep(8);
        }

        verify(acc, atLeast(1)).close();
    }
}
