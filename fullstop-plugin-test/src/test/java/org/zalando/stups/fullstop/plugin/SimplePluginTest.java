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

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

/**
 * @author jbellmann
 */
public class SimplePluginTest {

    @Test
    public void simplePluginTest() throws CallbackException {

        // set up your plugin
        ViolationSink violationSink = Mockito.mock(ViolationSink.class);
        SimplePlugin sp = new SimplePlugin(violationSink);

        // setup the processor with your plugin
        LocalPluginProcessor lpp = new LocalPluginProcessor(sp);

        // run processing with file from classpath
        lpp.processEvents(getClass().getResourceAsStream("/record.json"));

        // verify violations went through the 'violation-sink'
        verify(violationSink, atLeast(1)).put(Mockito.any());
    }

}
