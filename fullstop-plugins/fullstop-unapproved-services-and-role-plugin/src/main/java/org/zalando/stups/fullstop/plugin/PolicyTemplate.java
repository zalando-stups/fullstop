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
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.internal.JsonPolicyReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

/**
 * Created by mrandi.
 */
public class PolicyTemplate {

    public static Policy fromClasspath(final String classpathResource) {
        String content = getResourceContent(classpathResource);

        JsonPolicyReader jsonPolicyReader = new JsonPolicyReader();
        return jsonPolicyReader.createPolicyFromJsonString(
                content);

    }

    protected static String getResourceContent(final String classpathResource) {
        try {
            return new String(
                    Files.readAllBytes(
                            java.nio.file.Paths.get(PolicyTemplate.class.getResource(classpathResource).toURI())));
        }
        catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
