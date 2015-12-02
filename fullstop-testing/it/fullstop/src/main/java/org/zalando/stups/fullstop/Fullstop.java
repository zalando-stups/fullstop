package org.zalando.stups.fullstop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnablePluginRegistries({ FullstopPlugin.class })
@EnableWebSecurity
@EnableScheduling
public class Fullstop {

    @Autowired
    private RegisteredPluginLogger registeredPluginLogger;

    public static void main(final String[] args) {
        SpringApplication.run(Fullstop.class, args);
    }

    @PostConstruct
    public void init() {
        registeredPluginLogger.logRegisteredPlugins();
    }
}
