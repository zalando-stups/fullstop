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

package org.zalando.stups.fullstop.plugin;

import static java.lang.String.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.assertj.core.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zalando.stups.fullstop.plugin.config.ApplicationMasterdataPluginProperties;

public class UrlValidator implements Validator {

    private List<String> protocolsAllowed;

    private boolean privateHostsAllowed;

    public UrlValidator(List<String> protocolsAllowed, boolean privateHostsAllowed) {
        this.privateHostsAllowed = privateHostsAllowed;
        this.protocolsAllowed = protocolsAllowed;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return String.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String urlString = (String) target;
        if (Strings.isNullOrEmpty(urlString)) {
            return;
        }
        URL url;
        try {
            url = new URL(urlString);
        }
        catch (MalformedURLException ex) {
            // reject
            errors.rejectValue(null,
                               "malformedUrl",
                               format("%s is not a valid URL",
                                      urlString));
            return;
        }
        if (!privateHostsAllowed) {
            // check for localhost, 127.0.0.1 etc
            String host = url.getHost()
                             .toLowerCase();
            if (host.equals("localhost") || host.equals("127.0.0.1")) {
                // reject
                errors.rejectValue(null,
                                   "privateHost",
                                   format("%s is on a private host: %s",
                                          urlString,
                                          host));
            }
        }
        if (!protocolsAllowed.isEmpty()) {
            // check for allowed protocols
            String protocol = url.getProtocol()
                                 .toLowerCase();
            if (!protocolsAllowed.contains(protocol)) {
                errors.rejectValue(null,
                                   "illegalProtocol",
                                   format("%s has an illegal protocol: %s",
                                          urlString,
                                          protocol));
            }
        }
    }
}
