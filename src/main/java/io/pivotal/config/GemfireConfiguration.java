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
