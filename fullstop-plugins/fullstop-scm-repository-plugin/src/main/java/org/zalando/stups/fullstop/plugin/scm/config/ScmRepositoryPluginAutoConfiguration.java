/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin.scm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.kontrolletti.KontrollettiOperations;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.scm.ScmRepositoryPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

@Configuration
public class ScmRepositoryPluginAutoConfiguration {

    @Bean
    public ScmRepositoryPlugin scmRepositoryPlugin(
            final ViolationSink violationSink,
            final KioOperations kioOperations,
            final PieroneOperations pieroneOperations,
            KontrollettiOperations kontrollettiOperations, final UserDataProvider userDataProvider) {
        return new ScmRepositoryPlugin(
                violationSink, kioOperations, pieroneOperations,
                kontrollettiOperations,
                userDataProvider);
    }
}
