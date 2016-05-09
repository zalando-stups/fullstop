package org.zalando.stups.fullstop.filereader;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.ProcessingLibraryException;
import com.amazonaws.services.cloudtrail.processinglibrary.impl.DefaultEventFilter;
import com.amazonaws.services.cloudtrail.processinglibrary.impl.DefaultExceptionHandler;
import com.amazonaws.services.cloudtrail.processinglibrary.interfaces.EventFilter;
import com.amazonaws.services.cloudtrail.processinglibrary.interfaces.EventsProcessor;
import com.amazonaws.services.cloudtrail.processinglibrary.interfaces.ExceptionHandler;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailLog;
import com.amazonaws.services.cloudtrail.processinglibrary.progress.ProgressInfo;
import com.amazonaws.services.cloudtrail.processinglibrary.progress.ProgressState;
import com.amazonaws.services.cloudtrail.processinglibrary.progress.ProgressStatus;
import com.amazonaws.services.cloudtrail.processinglibrary.serializer.DefaultEventSerializer;
import com.amazonaws.services.cloudtrail.processinglibrary.serializer.EventSerializer;
import com.amazonaws.services.cloudtrail.processinglibrary.serializer.RawLogDeliveryEventSerializer;
import com.amazonaws.services.cloudtrail.processinglibrary.utils.EventBuffer;
import com.amazonaws.services.cloudtrail.processinglibrary.utils.LibraryUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * So we can test a bit as long no access is possible to the s3-files directly.
 *
 * @author jbellmann
 */
public class FileEventReader {

    private static final Logger logger = LoggerFactory.getLogger(FileEventReader.class);

    private final EventFilter eventFilter;

    private final EventsProcessor eventsProcessor;

    private final ExceptionHandler exceptionHandler;

    private final ObjectMapper mapper;

    private final boolean isEnableRawEventInfo = false;

    public FileEventReader(final EventsProcessor eventsProcessor, final EventFilter eventFilter) {
        this.eventsProcessor = eventsProcessor;
        this.eventFilter = eventFilter;
        this.mapper = new ObjectMapper();
        this.exceptionHandler = new DefaultExceptionHandler();
    }

    public FileEventReader(final EventsProcessor eventsProcessor) {
        this(eventsProcessor, new DefaultEventFilter());
    }

    public void readEvents(final File file, final CloudTrailLog ctLog) throws CallbackException {
        try {
            final GZIPInputStream gzippedInputStream = new GZIPInputStream(new FileInputStream(file));

            final EventSerializer serializer = this.getEventSerializer(gzippedInputStream, ctLog);

            this.emitEvents(serializer);
        }
        catch (IllegalArgumentException | IOException e) {
            this.exceptionHandler.handleException(
                    new ProcessingLibraryException(
                            e.getMessage(),
                            new ProgressStatus(ProgressState.parseMessage, new FakeProgressInfo())));
        }
    }

    /**
     * Get the EventSerializer based on user's configuration.
     *
     * @param inputStream the Gzipped content from CloudTrail log file
     * @param ctLog       CloudTrail log file
     * @return parser that parses CloudTrail log file
     * @throws java.io.IOException
     */
    private EventSerializer getEventSerializer(final GZIPInputStream inputStream, final CloudTrailLog ctLog)
            throws IOException {
        final EventSerializer serializer;

        if (isEnableRawEventInfo) {
            final String logFileContent = new String(LibraryUtils.toByteArray(inputStream), StandardCharsets.UTF_8);
            final JsonParser jsonParser = this.mapper.getFactory().createParser(logFileContent);
            serializer = new RawLogDeliveryEventSerializer(logFileContent, ctLog, jsonParser);
        }
        else {
            final JsonParser jsonParser = this.mapper.getFactory().createParser(inputStream);
            serializer = new DefaultEventSerializer(ctLog, jsonParser);
        }

        return serializer;
    }

    private void emitEvents(final EventSerializer serializer) throws CallbackException, IOException {
        final EventBuffer<CloudTrailEvent> eventBuffer = new EventBuffer<>(10);
        while (serializer.hasNextEvent()) {

            final CloudTrailEvent event = serializer.getNextEvent();

            if (this.eventFilter.filterEvent(event)) {
                eventBuffer.addEvent(event);

                if (eventBuffer.isBufferFull()) {
                    this.eventsProcessor.process(eventBuffer.getEvents());
                }

            }
            else {
                logger.debug("AWSCloudTrailEvent " + event + " has filtered.");
            }
        }

        // emit whatever in the buffer as last batch
        final List<CloudTrailEvent> events = eventBuffer.getEvents();
        if (!events.isEmpty()) {
            this.eventsProcessor.process(events);
        }
    }

    static class FakeProgressInfo implements ProgressInfo {

        @Override
        public boolean isSuccess() {
            return false;
        }

    }
}
