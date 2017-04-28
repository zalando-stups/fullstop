package org.zalando.stups.fullstop.plugin;

import com.unknown.pkg.ExampleApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author jbellmann
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleApplication.class)
public abstract class AbstractPluginTest {
}
