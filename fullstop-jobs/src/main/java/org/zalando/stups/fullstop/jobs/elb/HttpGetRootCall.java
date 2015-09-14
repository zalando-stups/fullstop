/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.jobs.elb;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Created by mrandi.
 */
public class HttpGetRootCall implements Callable<Boolean> {

    public static final String EMPTY_STRING = "";
    private final Logger log = LoggerFactory.getLogger(HttpGetRootCall.class);

    private final CloseableHttpClient httpclient;

    private final String host;

    private final Integer allowedPort;

    public HttpGetRootCall(CloseableHttpClient httpclient,
                           String host, Integer allowedPort) {
        this.httpclient = httpclient;
        this.host = host;
        this.allowedPort = allowedPort;
    }

    @Override
    public Boolean call() throws Exception {
        final String scheme = allowedPort == 443 ? "https" : "http";
        final URI uri = new URIBuilder()
                .setScheme(scheme)
                .setHost(host)
                .setPort(allowedPort)
                .build();

        log.debug("Checking URL: {}", uri);
        final HttpGet httpget = new HttpGet(uri);
        try (final CloseableHttpResponse response = httpclient.execute(httpget)) {
            final String location = Optional.ofNullable(response.getFirstHeader("Location"))
                    .map(Header::getValue)
                    .orElse(EMPTY_STRING);

            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 401 || statusCode == 403) {
                log.debug("URI {} is secured GET / returned {}", uri, statusCode);
                return true;
            } else if (String.valueOf(statusCode).startsWith("3") && location.startsWith("https")) {
                log.debug("URI {} redirects to an https location: {}", uri, location);
                return true;
            } else {
                log.info("URI {} is INSECURE. GET / returned {}", uri, response);
                return false;
            }
        } catch (final IOException e) {
            log.debug("URI {} threw exception {}", uri, e.toString());
            return true;
        }
    }
}
