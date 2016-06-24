package org.zalando.stups.fullstop.taupage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public final class TaupageYamlUtil {
    private TaupageYamlUtil() {
    }

    public static TaupageYaml parseTaupageYaml(String payload) {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(payload, TaupageYaml.class);
        } catch (IOException e) {
            return null;
        }
    }
}
