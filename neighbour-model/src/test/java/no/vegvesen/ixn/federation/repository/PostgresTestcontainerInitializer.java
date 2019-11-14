package no.vegvesen.ixn.federation.repository;

import org.junit.ClassRule;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestcontainerInitializer {
	private static final String FEDERATION = "federation";
	@ClassRule
	public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>("postgres:9.6")
			.withDatabaseName(FEDERATION)
			.withUsername(FEDERATION)
			.withPassword(FEDERATION);

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			postgresContainer.start();
			TestPropertyValues.of(
					"spring.datasource.url: jdbc:postgresql://localhost:" + postgresContainer.getMappedPort(5432) + "/federation",
					"spring.datasource.username: federation",
					"spring.datasource.password: federation",
					"spring.datasource.driver-class-name: org.postgresql.Driver"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
