/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.plugin.core.config.EnablePluginRegistries;

import org.zalando.stups.fullstop.plugin.FullstopPlugin;

/**
 * @author  jbellmann
 */
@SpringBootApplication
@EnablePluginRegistries({ FullstopPlugin.class })
@EnableJpaRepositories("org.zalando.stups.fullstop.violation.repository")
public class Fullstop {

    public static void main(final String[] args) {
        SpringApplication.run(Fullstop.class, args);
    }

    @Autowired
    private RegisteredPluginLogger registeredPluginLogger;

    @PostConstruct
    public void init() {
        registeredPluginLogger.logRegisteredPlugins();
    }
}
