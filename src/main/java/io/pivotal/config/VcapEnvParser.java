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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for reading the environment variable
 * <strong>VCAP_SERVICES</strong> present in Cloud Foundry environment.
 * <br></br>
 * This class has 2 main functions
 * <pre>
 *
 * 1. To parse VCAP_SERVICES environment variable
 * 2. To set security properties of Spring Data GemFire
 *</pre>
 *
 * @see <a href="https://docs.spring.io/spring-data/gemfire/docs/2.1.0.M1/reference/html/#bootstrap-annotation-config-security-client" >SDG docs</a>
 *
 * @author Pulkit Chandra
 *
 * @since 1.0
 */
public class VcapEnvParser implements EnvironmentPostProcessor {

    private final String vcapProperties;
    private static final String PROPERTY_SOURCE_NAME = "defaultProperties";

    private Map credentials = new HashMap();

    protected VcapEnvParser(String properties) {
        this.vcapProperties = properties;
    }

    public VcapEnvParser() {
        this.vcapProperties = System.getenv().get("VCAP_SERVICES");
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put("spring.data.gemfire.security.username",getCredentials().get("username"));
            map.put("spring.data.gemfire.security.password", getCredentials().get("password"));
            map.put("spring.data.gemfire.cache.pool.locators",getLocators());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (URISyntaxException e) {

        }
        addOrReplace(environment.getPropertySources(),map);
    }

    private void addOrReplace(MutablePropertySources propertySources,
                              Map<String, Object> map) {
        MapPropertySource target = null;
        if (propertySources.contains(PROPERTY_SOURCE_NAME)) {
            PropertySource<?> source = propertySources.get(PROPERTY_SOURCE_NAME);
            if (source instanceof MapPropertySource) {
                target = (MapPropertySource) source;
                for (String key : map.keySet()) {
                    if (!target.containsProperty(key)) {
                        target.getSource().put(key, map.get(key));
                    }
                }
            }
        }
        if (target == null) {
            target = new MapPropertySource(PROPERTY_SOURCE_NAME, map);
        }
        if (!propertySources.contains(PROPERTY_SOURCE_NAME)) {
            propertySources.addLast(target);
        }
    }

    private List<String> getLocators() throws IOException, URISyntaxException {
        Map credentials = getCredentials();
        List<String> locators = (List<String>) credentials.get("locators");
        return locators;
    }


    private Map getCredentials() throws IOException {
        if (credentials.size() == 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Map> services = objectMapper.readValue(this.vcapProperties, Map.class);
            Map gemfireService = getGemFireService(services);
            if (gemfireService != null) {
                credentials.put("locators", gemfireService.get("locators"));
                List<Map> users = (List) gemfireService.get("users");
                for (Map entry : users) {
                    List<String> roles = (List<String>) entry.get("roles");
                    if (roles.contains("cluster_operator")) {
                        credentials.put("username", entry.get("username"));
                        credentials.put("password", entry.get("password"));
                    }
                }
            }
        }
        return credentials;

    }

    private Map getGemFireService(Map services) {
        Map l = (Map) services.get("p-cloudcache");
        if (l == null) {
            throw new IllegalStateException("GemFire service is not bound to this application");
        }
        return l;
    }
}