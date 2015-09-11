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
package org.zalando.stups.fullstop.jobs.ec2;

import com.amazonaws.services.ec2.model.Instance;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

public class EC2HttpCall implements Callable<Boolean> {

    private final Logger log = LoggerFactory.getLogger(EC2HttpCall.class);

    private final CloseableHttpClient httpclient;

    private final Instance instance;

    private final Integer allowedPort;

    public EC2HttpCall(CloseableHttpClient httpclient, Instance instance,
                       Integer allowedPort) {
        this.httpclient = httpclient;
        this.instance = instance;
        this.allowedPort = allowedPort;
    }

    @Override
    public Boolean call() throws Exception {

        Boolean result = false;

        log.debug("Thread: {}", Thread.currentThread().getId());

        String scheme = allowedPort == 443 ? "https" : "http";

        if (allowedPort == 22 || allowedPort == 2222) {
            return true;
        }

        try {
            String canonicalHostedZoneName = instance.getPublicIpAddress();
            URI http = new URIBuilder().setScheme(scheme)
                    .setHost(canonicalHostedZoneName)
                    .setPort(allowedPort)
                    .build();
            HttpGet httpget = new HttpGet(http);
            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                if (response != null) {
                    String location = "";
                    for (Header header : response.getAllHeaders()) {
                        if (header.getName().equals("Location")) {
                            location = header.getValue();
                        }
                    }

                    if (response.getStatusLine().getStatusCode() == 401
                            || response.getStatusLine().getStatusCode() == 403) {
                        log.debug("thats ok - {}", canonicalHostedZoneName);
                        result = true;
                    } else if (String.valueOf(response.getStatusLine().getStatusCode()).startsWith("3")
                            && location.startsWith("https")) {
                        log.debug("thats ok - {}", canonicalHostedZoneName);
                        result = true;
                    } else {
                        log.debug("thats NOT ok - {}", canonicalHostedZoneName);
                        result = false;
                    }

                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }
}
