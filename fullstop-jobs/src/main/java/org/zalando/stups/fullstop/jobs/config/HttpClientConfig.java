package org.zalando.stups.fullstop.jobs.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class HttpClientConfig {

    @Bean
    @Scope("prototype")
    @SuppressWarnings("deprecation")
    public CloseableHttpClient build() {
        final RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(1000)
                .setConnectTimeout(1000)
                .setSocketTimeout(1000)
                .build();

        try {
            return HttpClientBuilder.create()
                    .disableAuthCaching()
                    .disableAutomaticRetries()
                    .disableConnectionState()
                    .disableCookieManagement()
                    .disableRedirectHandling()
                    .setDefaultRequestConfig(config)
                    .setUserAgent("fullstop-job (https://github.com/zalando-stups/fullstop)")
                    .setHostnameVerifier(new AllowAllHostnameVerifier())
                    .setSslcontext(
                            new SSLContextBuilder()
                                    .loadTrustMaterial(
                                            null,
                                            (arrayX509Certificate, value) -> true)
                                    .build())
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("Could not initialize httpClient", e);
        }
    }

}
