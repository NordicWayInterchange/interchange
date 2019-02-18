package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Primary
@Component
public class MockGeoLookup implements GeoLookup {

	@Override
	public List<String> getCountries(double lat, double lon) {
		return Arrays.asList("NO");
	}
}
