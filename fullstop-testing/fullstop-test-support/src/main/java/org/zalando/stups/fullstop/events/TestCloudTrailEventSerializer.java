package org.zalando.stups.fullstop.events;


import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventMetadata;
import com.amazonaws.services.cloudtrail.processinglibrary.serializer.AbstractEventSerializer;
import com.amazonaws.services.cloudtrail.processinglibrary.serializer.EventSerializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

public class TestCloudTrailEventSerializer extends AbstractEventSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public TestCloudTrailEventSerializer(JsonParser parser) throws IOException {
        super(parser);

        // this initializes the AbstractEventSerializer
        readArrayHeader();
    }

    public static CloudTrailEvent createCloudTrailEvent(final String resource) {
        try (final EventSerializer serializer = new TestCloudTrailEventSerializer(getParser(resource))) {
            // calling hasNextEvent() is essential before getNextEvent()
            checkState(serializer.hasNextEvent(), "Resource %s does not contain cloud trail events");
            return serializer.getNextEvent();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonParser getParser(String resource) throws IOException, URISyntaxException {
        return OBJECT_MAPPER.getFactory().createParser(readAllBytes(get(TestCloudTrailEventSerializer.class.getResource(resource).toURI())));
    }

    @Override
    public CloudTrailEventMetadata getMetadata(int charStart, int charEnd) {
        return null;
    }
}
