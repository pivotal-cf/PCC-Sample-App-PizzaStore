package io.pivotal.config;


import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class VcapEnvParserTest {


    @Rule
    public final ClearSystemProperties myPropertyIsCleared
            = new ClearSystemProperties("VCAP_SERVICES");


    @Rule
    public final ProvideSystemProperty myProp = new ProvideSystemProperty(
            "VCAP_SERVICES",new String(Files.readAllBytes(Paths.get("src/test/resources/VCAP_SERVICES.json")),"UTF-8"));


    public VcapEnvParserTest() throws IOException {
    }


    private final ConfigurableEnvironment env = new StandardEnvironment();
    @Test
    public void getLocators() throws IOException, URISyntaxException {
        VcapEnvParser envParcer = new VcapEnvParser(System.getProperty("VCAP_SERVICES"));

        envParcer.postProcessEnvironment(env, new SpringApplication(VcapEnvParserTest.class));

        MatcherAssert.assertThat(env.getProperty("spring.data.gemfire.security.username"),
                CoreMatchers.containsString("cluster"));
    }

    @Test
    public void getUsername() {
    }

    @Test
    public void getPasssword() {
    }
}