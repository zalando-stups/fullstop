package org.zalando.stups.fullstop.taupage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public final class TaupageYamlUtil {
    private static final Logger LOG = getLogger(TaupageYamlUtil.class);

    private TaupageYamlUtil() {
    }

    public static TaupageYaml parseTaupageYaml(String payload) {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(payload, TaupageYaml.class);
        } catch (IOException e) {
            LOG.warn("Could not parse taupage yaml: {}", e.toString());
            return null;
        }
    }
}
