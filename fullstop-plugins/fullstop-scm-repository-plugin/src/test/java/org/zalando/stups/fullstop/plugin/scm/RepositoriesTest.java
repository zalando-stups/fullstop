package org.zalando.stups.fullstop.plugin.scm;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.zalando.stups.fullstop.plugin.scm.config.ScmRepositoryPluginProperties;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(Parameterized.class)
public class RepositoriesTest {

    private final String url;
    private final Repository expectedRepository;
    private final Repositories repositories;

    public RepositoriesTest(Map<String, String> hosts, String url, Repository expectedRepository) {
        this.url = url;
        this.expectedRepository = expectedRepository;
        final ScmRepositoryPluginProperties properties = new ScmRepositoryPluginProperties();
        properties.setHosts(hosts);
        this.repositories = new Repositories(properties);
    }

    @Test
    public void testParse() throws Exception {
        Repository parsed = null;
        UnknownScmUrlException e = null;

        try {
            parsed = repositories.parse(url);
        } catch (UnknownScmUrlException ex) {
            e = ex;
        }

        if (expectedRepository == null) {
            if (e == null) {
                failBecauseExceptionWasNotThrown(UnknownScmUrlException.class);
            }
        } else {
            assertThat(parsed).isEqualTo(expectedRepository);
        }
    }

    @Parameters(name = "{1} via {0}")
    public static List<Object[]> data() {
        return asList(
                new Object[][]{
                        {singletonMap("github.com", "^.+$"), "https://github.com/zalando-stups/fullstop", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "https://GitHub.com/zAlAnDo-STUPS/FULLstop", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "https://github.com:443/zalando-stups/fullstop", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "git:https://github.com/zalando-stups/fullstop", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "http://github.com/zalando-stups/fullstop", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "https://github.com/zalando-stups/fullstop.git", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "https://github.com/zalando-stups/fullstop.git/src", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "https://github.com/zalando-stups/fullstop/tree/master/src", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "git@github.com:zalando-stups/fullstop", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "git@github.com:zalando-stups/fullstop.git", new Repository("github.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "git:git@github.com:zalando-stups/fullstop.git", new Repository("github.com", "zalando-stups", "fullstop")},
                        {ImmutableMap.of("github.com", "^.+$", "git.my.company.com", "^.+$"), "https://github.com/zalando-stups/fullstop", new Repository("github.com", "zalando-stups", "fullstop")},
                        {ImmutableMap.of("github.com", "^.+$", "git.my.company.com", "^.+$"), "https://git.my.company.com/zalando-stups/fullstop", new Repository("git.my.company.com", "zalando-stups", "fullstop")},
                        {singletonMap("github.com", "^.+$"), "https://git.my.company.com/zalando-stups/fullstop", null},
                        {singletonMap("github.com", "^.+$"), "https://github.com/zalando-stups", null},
                        {singletonMap("github.com", "^.+$"), "https://github.com", null}
                }
        );
    }
}
