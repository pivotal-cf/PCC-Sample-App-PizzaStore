package io.pivotal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;

@SpringBootApplication
@EnableCaching
public class CloudcachePizzaStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudcachePizzaStoreApplication.class, args);
    }
}
