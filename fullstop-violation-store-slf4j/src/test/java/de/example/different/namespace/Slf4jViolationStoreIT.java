/**
 * Copyright 2015 Zalando SE
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
package de.example.different.namespace;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.stups.fullstop.violation.ViolationStore;
import org.zalando.stups.fullstop.violation.entity.Violation;
import org.zalando.stups.fullstop.violation.entity.ViolationBuilder;
import org.zalando.stups.fullstop.violation.store.slf4j.Slf4jViolationStore;
import org.zalando.stups.fullstop.violation.store.slf4j.Slf4jViolationStoreProperties;

/**
 * Simple Test.
 *
 * @author jbellmann
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
public class Slf4jViolationStoreIT {

    @Autowired
    private ViolationStore violationStore;

    @Autowired
    private Slf4jViolationStoreProperties properties;

    @Test
    public void testViolationStoreCreation() {

        Assertions.assertThat(properties.getLoggernames()).contains("fullstop.violations.store");

        Assertions.assertThat(violationStore).isNotNull();
        Assertions.assertThat(violationStore.getClass()).isEqualTo(Slf4jViolationStore.class);

        Violation violation = new ViolationBuilder("JUST A TEST").withAccoundId("ACCOUNT_ID").withRegion("REGION")
                .build();
        this.violationStore.save(violation);
    }
}
