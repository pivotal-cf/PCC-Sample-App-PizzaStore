package io.pivotal.gemfire.server;

import java.io.Serializable;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.geode.config.annotation.UseGroups;
import org.springframework.geode.security.support.SecurityManagerSupport;
import org.springframework.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * The BootGemFireServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "BootGemFireServerApplication")
@EnableLocator
@EnableManager
@EnablePdx
@SuppressWarnings("unused")
public class BootGemFireServerApplication {

	private static final String GEMFIRE_SECURITY_USERNAME_PROPERTY = "security-username";
	private static final String GEMFIRE_SECURITY_PASSWORD_PROPERTY = "security-password";
	private static final String SPRING_SECURITY_USERNAME_PROPERTY = "spring.data.gemfire.security.username";
	private static final String SPRING_SECURITY_PASSWORD_PROPERTY = "spring.data.gemfire.security.password";

	public static void main(String[] args) {

		new SpringApplicationBuilder(BootGemFireServerApplication.class)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Profile("local")
	@UseGroups("local")
	static class LocalGemFireConfiguration { }

	@Bean("Name")
	public PartitionedRegionFactoryBean<Object, Object> nameRegion(GemFireCache gemfireCache) {

		PartitionedRegionFactoryBean<Object, Object> nameRegion = new PartitionedRegionFactoryBean<>();

		nameRegion.setCache(gemfireCache);
		nameRegion.setClose(false);
		nameRegion.setPersistent(false);

		return nameRegion;
	}

	@Bean("Pizza")
	public PartitionedRegionFactoryBean<Object, Object> pizzaRegion(GemFireCache gemfireCache) {

		PartitionedRegionFactoryBean<Object, Object> pizzaRegion = new PartitionedRegionFactoryBean<>();

		pizzaRegion.setCache(gemfireCache);
		pizzaRegion.setClose(false);
		pizzaRegion.setPersistent(false);

		return pizzaRegion;
	}

	@Bean
	org.apache.geode.security.SecurityManager testSecurityManager(Environment environment) {
		return new TestSecurityManager(environment);
	}

	static class TestSecurityManager extends SecurityManagerSupport {

		private final Environment environment;

		TestSecurityManager(Environment environment) {

			Assert.notNull(environment, "Environment must not be null");

			this.environment = environment;
		}

		Environment getEnvironment() {
			return this.environment;
		}

		@Override
		public Object authenticate(Properties credentials) throws AuthenticationFailedException {

			Environment environment = getEnvironment();

			String expectedUsername = Optional.of(environment)
				.filter(env -> env.containsProperty(SPRING_SECURITY_USERNAME_PROPERTY))
				.map(env -> env.getProperty(SPRING_SECURITY_USERNAME_PROPERTY))
				.orElseGet(() -> UUID.randomUUID().toString());

			String expectedPassword = Optional.of(environment)
				.filter(env -> env.containsProperty(SPRING_SECURITY_PASSWORD_PROPERTY))
				.map(env -> env.getProperty(SPRING_SECURITY_PASSWORD_PROPERTY))
				.orElse(expectedUsername);

			String actualUsername = credentials.getProperty(GEMFIRE_SECURITY_USERNAME_PROPERTY);
			String actualPassword = credentials.getProperty(GEMFIRE_SECURITY_PASSWORD_PROPERTY);

			//System.err.printf("Expected User is [%s]; Expected Password is [%s]%n", expectedUsername, expectedPassword);
			//System.err.printf("User was [%s]; Password was [%s]%n", actualUsername, actualPassword);

			if (!(expectedUsername.equals(actualUsername) && expectedPassword.equals(actualPassword))) {
				throw new AuthenticationFailedException(String.format("User [%s] is not authorized", actualUsername));
			}

			return User.of(actualUsername);
		}

		@Override
		public boolean authorize(Object principal, ResourcePermission permission) {
			return principal != null;
		}
	}

	@ToString(of = "name")
	@EqualsAndHashCode(of = "name")
	@RequiredArgsConstructor(staticName = "of")
	static class User implements Serializable {

		@NonNull @Getter
		private final String name;

	}
}
