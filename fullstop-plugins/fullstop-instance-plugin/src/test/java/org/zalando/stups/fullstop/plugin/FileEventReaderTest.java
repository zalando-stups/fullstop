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

import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.AmazonEC2Client;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.zalando.stups.fullstop.aws.ClientProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jbellmann
 */
@Ignore
public class FileEventReaderTest {

    private PluginRegistry<FullstopPlugin, CloudTrailEvent> pluginRegistry;

    private ClientProvider clientProvider;

    private RunInstancePlugin plugin;

    @Before
    public void setUp() {
        clientProvider = Mockito.mock(ClientProvider.class);

        List<FullstopPlugin> plugins = new ArrayList<>();
        plugin = new RunInstancePlugin(null, null);
        plugins.add(plugin);
        pluginRegistry = SimplePluginRegistry.create(plugins);
    }

    @Test
    public void testReadLogFile() throws CallbackException {

        AmazonEC2Client client = Mockito.mock(AmazonEC2Client.class);
        Mockito.when(clientProvider.getClient(AmazonEC2Client.class, Mockito.anyString(), Mockito.any(Region.class)))
                .thenReturn(client);

// for (String filename : LogFiles.all()) {
//
// File file = new File(getClass().getResource("/logs/" + filename).getFile());
// CloudTrailLog ctLog = Mockito.mock(CloudTrailLog.class);
// FileEventReader reader = new FileEventReader(new PluginEventsProcessor(pluginRegistry));
// reader.readEvents(file, ctLog);
// }
    }
}
