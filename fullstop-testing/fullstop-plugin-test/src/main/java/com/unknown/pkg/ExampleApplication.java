package com.unknown.pkg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application for testing. Using 'com.unknown.pkg' helps to verify component-scan works as expected.
 *
 * @author jbellmann
 */
@SpringBootApplication
public class ExampleApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
