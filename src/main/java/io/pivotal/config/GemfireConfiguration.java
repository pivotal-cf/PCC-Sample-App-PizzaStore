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


import io.pivotal.model.Name;
import io.pivotal.model.Pizza;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.config.annotation.*;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.support.ConnectionEndpoint;

import java.net.URI;
import java.util.Properties;

@Configuration
@ClientCacheApplication(name = "GemFireSpringPizzaStoreApplication", durableClientId = "pizza-store", keepAlive = true, readyForEvents = true, subscriptionEnabled = true)
@EnableContinuousQueries(poolName = "DEFAULT")
@EnableEntityDefinedRegions(basePackageClasses = {Pizza.class, Name.class})
@EnableGemfireCaching
@EnableGemfireRepositories("io.pivotal.repository.gemfire")
@EnableSecurity
public class GemfireConfiguration {

    private static final String SECURITY_CLIENT = "security-client-auth-init";
    private static final String SECURITY_USERNAME = "security-username";
    private static final String SECURITY_PASSWORD = "security-password";

    @Bean
    ClientCacheConfigurer clientCacheSecurityConfigurer() {

        return (beanName, clientCacheFactoryBean) -> {

            Cloud cloud = new CloudFactory().getCloud();
            ServiceInfo serviceInfo = (ServiceInfo) cloud.getServiceInfos().get(0);
            Properties gemfireProperties = clientCacheFactoryBean.getProperties();

            gemfireProperties.setProperty(SECURITY_USERNAME, serviceInfo.getUsername());
            gemfireProperties.setProperty(SECURITY_PASSWORD, serviceInfo.getPassword());
            gemfireProperties.setProperty(SECURITY_CLIENT, "io.pivotal.config.UserAuthInitialize.create");

            for (URI locator : serviceInfo.getLocators()) {
                clientCacheFactoryBean.addLocators(new ConnectionEndpoint(locator.getHost(), locator.getPort()));
            }

            clientCacheFactoryBean.setProperties(gemfireProperties);
            clientCacheFactoryBean.setPdxSerializer(
                    new ReflectionBasedAutoSerializer("io.pivotal.model.Pizza", "io.pivotal.model.Name"));
        };
    }

}
