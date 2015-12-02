package com.unknown;

import com.unknown.comp.SimpleDemonstrationViolationHandler;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;
import reactor.bus.EventBus;
import reactor.bus.selector.Selectors;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Testing autoconfiguration.
 *
 * @author jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleApplication.class)
@WebIntegrationTest
public class ViolationSinkIT {

    @Autowired
    private ViolationSink violationSink;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private SimpleDemonstrationViolationHandler handler;

    private CountDownLatch latch;

    @Before
    public void setUp() {
        latch = new CountDownLatch(1);
        eventBus.on(
                Selectors.$("/violations"),
                ev -> System.out.println(ev.getData()));
        eventBus.on(Selectors.$("/violations"), ev -> latch.countDown());
    }

    @Test
    public void handleViolation() throws InterruptedException {

        ViolationBuilder vBuilder = new ViolationBuilder();
        vBuilder.withPluginFullyQualifiedClassName(ViolationSinkIT.class);
        vBuilder.withAccountId("1234567");
        vBuilder.withEventId(UUID.randomUUID().toString());
        vBuilder.withRegion("sig-west-13");
        vBuilder.withMetaInfo("{\"key\": \"value\"}");

        violationSink.put(vBuilder.build());

        latch.await(5, TimeUnit.SECONDS);

        int count = handler.getCount();
        Assertions.assertThat(count).isGreaterThan(0);
    }
}
