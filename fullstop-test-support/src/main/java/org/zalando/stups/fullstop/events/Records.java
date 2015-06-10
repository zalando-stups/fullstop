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
package org.zalando.stups.fullstop.events;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author jbellmann
 */
public class Records {

    public static List<Map<String, Object>> fromClasspath(final String classpathResource) {
        String content = getResourceContent(classpathResource);
        List<Map<String, Object>> result = Lists.newArrayList();
        JSONArray jsonArray = JsonPath.read(content, "$.Records");
        Iterator<Object> iter = jsonArray.iterator();
        while (iter.hasNext()) {
            Map<String, Object> nextObject = (Map<String, Object>) iter.next();
            result.add(nextObject);
        }

        return result;
    }

    protected static String getResourceContent(final String classpathResource) {
        try {
            return new String(Files.readAllBytes(
                    java.nio.file.Paths.get(Records.class.getResource(classpathResource).toURI())));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
