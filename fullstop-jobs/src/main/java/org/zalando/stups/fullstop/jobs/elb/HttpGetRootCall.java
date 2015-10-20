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
import org.zalando.stups.fullstop.jobs.common.HttpCallResult;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Created by mrandi.
 */
public class HttpGetRootCall implements Callable<HttpCallResult> {

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
    public HttpCallResult call() throws Exception {
        HttpCallResult callResult = new HttpCallResult(false, "");
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
                callResult.setSecured(true);
            } else if (String.valueOf(statusCode).startsWith("3")) {
                if (location.startsWith("https")) {
                    log.debug("URI {} redirects to an https location: {}", uri, location);
                    callResult.setSecured(true);
                } else {
                    log.debug("Call to {} redirects (status {}) to location with unsafe protocol ({})", uri, statusCode, location);
                    callResult.setMessage(String.format("Call to %s redirects (status %d) to location with unsafe protocol (%s)", uri, statusCode, location));
                }

            } else if (String.valueOf(statusCode).startsWith("5")) {
                log.info("URI {} is SECURE. GET / returned {}", uri, response);
                callResult.setSecured(true);
            } else {
                callResult.setMessage(String.format("%s returned status code %d, which means it is unsecured", uri, statusCode));
            }

        } catch (final IOException e) {
            log.debug("URI {} threw exception {}", uri, e.toString());
            callResult.setSecured(true);
        }
        return callResult;
    }
}
