package org.zalando.stups.fullstop.taupage;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class TaupageYamlUtilTest {

    @Test
    public void parseTaupageYaml() throws Exception {
        final TaupageYaml taupageYaml = TaupageYamlUtil.parseTaupageYaml(loadContent("/taupage01.yaml"));
        assertThat(taupageYaml).isNotNull();
        assertThat(taupageYaml.getApplicationId()).isEqualTo("fullstop");
        assertThat(taupageYaml.getApplicationVersion()).isEqualTo("5");
    }

    @Test
    public void testParseMissingAppVersion() throws Exception {
        final TaupageYaml taupageYaml = TaupageYamlUtil.parseTaupageYaml(loadContent("/taupage-missing-app-version.yaml"));
        assertThat(taupageYaml).isNotNull();
        assertThat(taupageYaml.getApplicationId()).isEqualTo("fullstop");
        assertThat(taupageYaml.getApplicationVersion()).isNull();
    }

    private static String loadContent(String classPathResourceName) throws IOException {
        return IOUtils.toString(new ClassPathResource(classPathResourceName).getInputStream(), UTF_8);
    }
}
