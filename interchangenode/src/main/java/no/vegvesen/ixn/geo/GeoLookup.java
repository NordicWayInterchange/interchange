package no.vegvesen.ixn.geo;

import java.util.List;

public interface GeoLookup {
	List<String> getCountries(double lat, double lon);
}
