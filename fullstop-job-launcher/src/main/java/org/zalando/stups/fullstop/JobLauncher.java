package org.zalando.stups.fullstop;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@Configuration
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ComponentScan
@EnableScheduling
public class JobLauncher {

    public static void main(String[] args) {
        run(JobLauncher.class, args);
    }
}
