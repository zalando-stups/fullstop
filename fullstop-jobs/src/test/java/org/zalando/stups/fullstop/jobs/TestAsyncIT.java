package org.zalando.stups.fullstop.jobs;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class TestAsyncIT {

    private final Logger log = LoggerFactory.getLogger(TestAsyncIT.class);

    private final Set<Integer> allowedPorts = newHashSet(443, 80);

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    private final RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(1000)
            .setConnectTimeout(1000)
            .setSocketTimeout(1000)
            .build();

    private CloseableHttpClient httpclient;

    @Test
    public void run() throws InterruptedException {

        threadPoolTaskExecutor.setCorePoolSize(8);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(100);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setKeepAliveSeconds(30);
        threadPoolTaskExecutor.setThreadGroupName("elb-check-group");
        threadPoolTaskExecutor.setThreadNamePrefix("elb-check-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setDaemon(true);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.afterPropertiesSet();

        try {
            httpclient = HttpClientBuilder.create()
                    .disableAuthCaching()
                    .disableAutomaticRetries()
                    .disableConnectionState()
                    .disableCookieManagement()
                    .disableRedirectHandling()
                    .setDefaultRequestConfig(config)
                    .setHostnameVerifier(new AllowAllHostnameVerifier())
                    .setSslcontext(
                            new SSLContextBuilder()
                                    .loadTrustMaterial(
                                            null,
                                            (arrayX509Certificate, value) -> true)
                                    .build())
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error(e.getMessage(), e);
        }

        final List<String> addresses = newArrayList(
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com",
                "www.google.de", "www.google.it", "www.google.com");

        for (final String address : addresses) {

            for (final Integer allowedPort : allowedPorts) {

                final HttpCall httpCall = new HttpCall(httpclient, address, allowedPort);
                final ListenableFuture<Void> listenableFuture = threadPoolTaskExecutor.submitListenable(httpCall);
                listenableFuture.addCallback(
                        new SuccessCallback<Void>() {
                            @Override
                            public void onSuccess(final Void result) {
                                log.info("address: {} and port: {}", address, allowedPort);
                            }
                        }, new FailureCallback() {
                            @Override
                            public void onFailure(final Throwable ex) {
                                log.warn(ex.getMessage(), ex);
                            }
                        });

                log.info("getActiveCount: {}", threadPoolTaskExecutor.getActiveCount());
                log.info("### - Thread: {}", Thread.currentThread().getId());
            }
        }

        //TODO: important use this to let the test run all thread!
        //TimeUnit.MINUTES.sleep(5);
    }

    static class HttpCall implements Callable<Void> {

        private final Logger log = LoggerFactory.getLogger(HttpCall.class);

        private final CloseableHttpClient httpclient;

        private final String address;

        private final Integer allowedPort;

        public HttpCall(final CloseableHttpClient httpclient, final String address, final Integer allowedPort) {
            this.httpclient = httpclient;
            this.address = address;
            this.allowedPort = allowedPort;
        }

        @Override
        public Void call() throws Exception {
            log.info("Thread: {}", Thread.currentThread().getId());

            final String scheme = allowedPort == 443 ? "https" : "http";

            try {
                final URI http = new URIBuilder().setScheme(scheme)
                        .setHost(address)
                        .setPort(allowedPort)
                        .build();
                final HttpGet httpget = new HttpGet(http);
                try (CloseableHttpResponse response = httpclient.execute(httpget)) {


                    if (response != null) {
                        String location = "";
                        for (final Header header : response.getAllHeaders()) {
                            if (header.getName().equals("Location")) {
                                location = header.getValue();
                            }
                        }

                        if (response.getStatusLine().getStatusCode() == 401
                                || response.getStatusLine().getStatusCode() == 403
                                || response.getStatusLine().getStatusCode() == 404) {
                            log.info("thats ok - {}", address);
                        } else if (String.valueOf(response.getStatusLine().getStatusCode()).startsWith("3")
                                && location.startsWith("https")) {
                            log.info("thats ok - {}", address);
                        } else {
                            log.info("thats NOT ok - {}", address);
                        }

                    }
                } catch (final IOException e) {
                    log.error(e.getMessage());
                }
            } catch (final URISyntaxException e) {
                log.error(e.getMessage());
            }
            return null;
        }
    }

}
