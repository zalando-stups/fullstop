package org.zalando.stups.fullstop.jobs.config;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;


@ContextConfiguration(classes = HttpClientConfig.class)
public class HttpClientConfigTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final WireMockRule wireMock = new WireMockRule(options().port(8898));

    @Autowired
    private HttpClient httpClient;

    @Test(expected = SocketTimeoutException.class)
    public void testHttpClient() throws IOException {
        stubFor(get("/hello").willReturn(aResponse().withStatus(200).but().withFixedDelay(2000)));

        httpClient.execute(new HttpGet("http://localhost:8898/hello"));
    }
}
