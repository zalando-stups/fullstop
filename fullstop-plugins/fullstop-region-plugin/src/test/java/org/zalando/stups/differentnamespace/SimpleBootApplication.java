package org.zalando.stups.differentnamespace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author jbellmann
 */
@SpringBootApplication
@EnableConfigurationProperties({ RegionPluginProperties.class })
public class SimpleBootApplication {

    @Autowired
    private RegionPluginProperties regionPluginProperties;

    public static void main(final String[] args) {
        SpringApplication.run(SimpleBootApplication.class, args);
    }

    @PostConstruct
    public void init() {
        final List<String> whitelistedRegions = regionPluginProperties.getWhitelistedRegions();

        System.out.println(whitelistedRegions.toString());
    }
}
