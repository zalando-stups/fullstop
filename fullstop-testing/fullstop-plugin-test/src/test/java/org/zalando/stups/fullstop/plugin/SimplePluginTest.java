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
        final ViolationSink violationSink = Mockito.mock(ViolationSink.class);
        final SimplePlugin sp = new SimplePlugin(violationSink);

        // setup the processor with your plugin
        final LocalPluginProcessor lpp = new LocalPluginProcessor(sp);

        // run processing with file from classpath
        lpp.processEvents(getClass().getResourceAsStream("/record.json"));

        // verify violations went through the 'violation-sink'
        verify(violationSink, atLeast(1)).put(Mockito.any());
    }

}
