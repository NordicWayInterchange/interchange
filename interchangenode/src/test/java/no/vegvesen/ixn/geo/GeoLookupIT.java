package no.vegvesen.ixn.geo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GeoLookupIT extends DockerBaseIT {

	@ClassRule
	public static GenericContainer postgisContainer = getPostgisContainer("interchangenode/src/test/docker/postgis");

	private GeoLookup geoLookup;

	@Before
	public void setup() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:postgresql://localhost:" + postgisContainer.getMappedPort(JDBC_PORT) + "/geolookup");
		config.setUsername("geolookup");
		config.setPassword("geolookup");
		config.setDriverClassName("org.postgresql.Driver");
		HikariDataSource ds = new HikariDataSource(config);
		geoLookup = new GeoLookup(new JdbcTemplate(ds));
	}

	@Test
	public void getPointAtBorderNorwaySwedenReturnsTwoCountries() {
		List<String> countries = geoLookup.getCountries(60d, 12.5d);
		assertThat(countries).hasSize(2).contains("SE").contains("NO");
	}

	@Test
	public void getPointInTheNorthSeaReturnsNoCountries() {
		List<String> countries = geoLookup.getCountries(60d, 4.0d);
		assertThat(countries).hasSize(0);
	}

}