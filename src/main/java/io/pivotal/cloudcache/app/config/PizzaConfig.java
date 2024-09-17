package io.pivotal.cloudcache.app.config;

import org.apache.geode.cache.client.SocketFactory;
import org.apache.geode.cache.client.proxy.SniProxySocketFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.support.RestTemplateConfigurer;
import org.springframework.geode.config.annotation.EnableClusterAware;
import org.springframework.geode.config.annotation.EnableDurableClient;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.io.File;

@Configuration
@EnableDurableClient(id = "pizza-store")
@EnableEntityDefinedRegions(basePackages = "io.pivotal.cloudcache.app.model")
@EnableClusterAware
public class PizzaConfig {

    @Profile({"off-platform", "app-foundation"})
    @Bean("mySocketFactory")
    SocketFactory getSocketFactoryBean(@Value("${service-gateway.hostname}") String hostname,
                                       @Value("${service-gateway.port}") int port) {
        SniProxySocketFactory factory = new SniProxySocketFactory(hostname, port);
            return factory;
    }

    @Profile({"app-foundation"})
    @Bean
    public RestTemplateConfigurer restTemplateConfigurer(ClientHttpRequestFactory factory) {
        return restTemplate -> restTemplate.setRequestFactory(factory);
    }

    @Profile({"app-foundation"})
    @Bean
    ClientHttpRequestFactory clientHttpRequestFactory(
            @Value("${gemfire.ssl-truststore}") String truststorePath,
            @Value("${gemfire.ssl-truststore-password}") String trustStorePassword
    ) throws Exception {
        SSLContext sslContext = SSLContextBuilder
                .create()
//   intentionally commented out. no mutual TLS wanted .loadKeyMaterial(ResourceUtils.getFile("classpath:keystore.jks"), allPassword.toCharArray(), allPassword.toCharArray())
                .loadTrustMaterial(new File(truststorePath), trustStorePassword.toCharArray())
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(
                        SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(sslContext)
                                .build()
                )
                .build();
        CloseableHttpClient client = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(client);

        return factory;
    }
}
