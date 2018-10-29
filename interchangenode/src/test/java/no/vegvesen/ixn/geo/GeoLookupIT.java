package no.vegvesen.ixn.geo;

import no.vegvesen.ixn.TestAutowireApp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestAutowireApp.class, GeoLookup.class})
@ActiveProfiles("it")
public class GeoLookupIT {

	@Autowired
	DataSource dataSource;

	private GeoLookup geoLookup;

	@Before
	public void setup() {
		geoLookup = new GeoLookup(new JdbcTemplate(dataSource));
	}

	@Test
	public void getPointAtBorderNorwaySwedenReturnsTwoCountries() {
		List<String> countries = geoLookup.getCountries(60f, 12.5f);
		assertThat(countries).hasSize(2).contains("SE").contains("NO");
	}

	@Test
	public void getPointInTheNorthSeaReturnsNoCountries() {
		List<String> countries = geoLookup.getCountries(60f, 4.0f);
		assertThat(countries).hasSize(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getPointInvalidStringThrowsException() {
		geoLookup.getCountries(";drop", "table");
	}
}