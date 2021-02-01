package io.pivotal.cloudcache.app.config;

import org.apache.geode.cache.client.SocketFactory;
import org.apache.geode.cache.client.proxy.SniProxySocketFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.support.RestTemplateConfigurer;
import org.springframework.geode.config.annotation.EnableDurableClient;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
@EnableDurableClient(id = "pizza-store")
@EnableEntityDefinedRegions(basePackages = "io.pivotal.cloudcache.app.model")
@EnableClusterConfiguration(useHttp = true)
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

        HttpClient client = HttpClients.custom()
                .setSslcontext(sslContext)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(client);

        return factory;
    }
}
