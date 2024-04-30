package no.vegvesen.ixn.postgresinit;

import org.junit.ClassRule;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestcontainerInitializer {
	private static final String FEDERATION = "federation";
	@ClassRule
	public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>("postgres:15")
			.withDatabaseName(FEDERATION)
			.withUsername(FEDERATION)
			.withPassword(FEDERATION);

	public static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			postgresContainer.start();
			TestPropertyValues.of(
					String.format("spring.datasource.url: %s", postgresContainer.getJdbcUrl()),
					"spring.datasource.username: " + FEDERATION,
					"spring.datasource.password: " + FEDERATION,
					"spring.datasource.driver-class-name: " + postgresContainer.getDriverClassName(),
					"spring.jpa.hibernate.ddl-auto : create-drop"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
