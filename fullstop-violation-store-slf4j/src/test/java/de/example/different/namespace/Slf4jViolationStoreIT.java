package de.example.different.namespace;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.zalando.stups.fullstop.violation.ViolationStore;
import org.zalando.stups.fullstop.violation.store.slf4j.Slf4jViolationStore;

/**
 * @author  jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class Slf4jViolationStoreIT {

    @Autowired
    private ViolationStore violationStore;

    @Test
    public void testViolationStoreCreation() {
        Assertions.assertThat(violationStore).isNotNull();
        Assertions.assertThat(violationStore.getClass()).isEqualTo(Slf4jViolationStore.class);
        this.violationStore.save("JUST A TEST");
    }
}
