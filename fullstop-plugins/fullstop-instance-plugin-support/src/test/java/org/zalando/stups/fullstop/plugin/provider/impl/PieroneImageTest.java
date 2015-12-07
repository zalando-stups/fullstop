package org.zalando.stups.fullstop.plugin.provider.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;

public class PieroneImageTest {

    private static final String REPOSITORY = "pierone.example.org";
    private static final String TEAM = "team";
    private static final String ARTIFACT = "artifact";
    private static final String TAG = "0.1.0.5.0-7.8";
    private PieroneImage pieroneImage;

    @Before
    public void setUp() throws Exception {
        pieroneImage = new PieroneImage(REPOSITORY, TEAM, ARTIFACT, TAG);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testTryParse() throws Exception {
        Optional<PieroneImage> result = PieroneImage.tryParse(get(REPOSITORY, TEAM, ARTIFACT).toString()+":"+TAG);

        assertThat(result).isPresent();

        assertThat(result.get().getRepository()).isEqualTo(REPOSITORY);
        assertThat(result.get().getTeam()).isEqualTo(TEAM);
        assertThat(result.get().getArtifact()).isEqualTo(ARTIFACT);
        assertThat(result.get().getTag()).isEqualTo(TAG);

        assertThat(pieroneImage.toString()).isEqualTo(result.get().toString());

    }

    @Test
    public void testGetTag() throws Exception {
        assertThat(pieroneImage.getTag()).isEqualTo(TAG);
    }

    @Test
    public void testGetArtifact() throws Exception {
        assertThat(pieroneImage.getArtifact()).isEqualTo(ARTIFACT);
    }

    @Test
    public void testGetTeam() throws Exception {
        assertThat(pieroneImage.getTeam()).isEqualTo(TEAM);
    }

    @Test
    public void testGetRepository() throws Exception {
        assertThat(pieroneImage.getRepository()).isEqualTo(REPOSITORY);
    }
}