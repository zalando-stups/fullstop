package org.zalando.stups.differentnamespace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

/**
 * @author jbellmann
 */
@SpringBootApplication
@EnablePluginRegistries({ FullstopPlugin.class })
public class FullstopApplication {

    public static void main(final String[] args) {
        SpringApplication.run(FullstopApplication.class, args);
    }

}
