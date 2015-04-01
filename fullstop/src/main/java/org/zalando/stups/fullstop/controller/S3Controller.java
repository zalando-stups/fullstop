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

package org.zalando.stups.fullstop.controller;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.PluginEventsProcessor;
import org.zalando.stups.fullstop.filereader.FileEventReader;
import org.zalando.stups.fullstop.plugin.AmiPlugin;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by gkneitschel.
 */
@RestController
@RequestMapping(value = "/s3reader", produces = APPLICATION_JSON_VALUE)
public class S3Controller {
    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    @RequestMapping(method = GET)
    public void fetchS3() throws CallbackException, FileNotFoundException {

        File directory = new File(getClass().getResource("/logs/").getFile());

        File[] files = directory.listFiles();

        if (files == null) {
            throw new FileNotFoundException("Directory is empty");
        }

        List<FullstopPlugin> plugins = new ArrayList<>();
        plugins.add(new AmiPlugin());
        pluginRegistry = SimplePluginRegistry.create(plugins);


        for (File file : files) {
            FileEventReader reader = new FileEventReader(new PluginEventsProcessor(pluginRegistry));
            reader.readEvents(file, null);
        }
    }
}
