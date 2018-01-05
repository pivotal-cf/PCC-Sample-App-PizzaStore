/*
 * Copyright (C) 2018-Present Pivotal Software, Inc. All rights reserved.
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.config;

import org.springframework.cloud.service.BaseServiceInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceInfo extends BaseServiceInfo {

    private final Pattern p = Pattern.compile("(.*)\\[(\\d*)\\]");

    private URI[] locators;
    private User user;

    public ServiceInfo(String id, List<String> locators, List<Map<String, Object>> users) {
        super(id);

        parseLocators(locators);
        parseUsers(users);
    }

    private void parseLocators(List<String> locators) {
        ArrayList<URI> uris = new ArrayList<URI>(locators.size());

        for (String locator : locators) {
            uris.add(parseLocator(locator));
        }

        this.locators = uris.toArray(new URI[uris.size()]);
    }

    private URI parseLocator(String locator) throws IllegalArgumentException {
        Matcher m = p.matcher(locator);
        if (!m.find()) {
            throw new IllegalArgumentException("Could not parse locator url. Expected format host[port], received: " + locator);
        } else {
            if (m.groupCount() != 2) {
                throw new IllegalArgumentException("Could not parse locator url. Expected format host[port], received: " + locator);
            }
            try {
                return new URI("locator://" + m.group(1) + ":" + m.group(2));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Malformed URL " + locator);
            }
        }
    }

    private void parseUsers(List<Map<String, Object>> users) {
        if (users == null) return;

        for (Map<String, Object> map : users) {
            User user = new User(map);

            if (user.isClusterOperator()) {
                this.user = user;
                break;
            }
        }
    }

    public URI[] getLocators() {
        return locators;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

}

