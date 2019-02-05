import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import org.assertj.core.data.Percentage;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DistanceTest {
	private static final double NAUTIC_MILE = 1852;
	private static final Percentage TOLERANCE = Percentage.withPercentage(1);

	@Test
	public void testOneDegreeIsApproxiamtely60NauticalMiles() {
		GeodesicData g = Geodesic.WGS84.Inverse((double) 12, (double) 12, (double) 13, (double) 12);
		double oneDegreeInMeters = g.s12;
		assertThat(oneDegreeInMeters).isCloseTo((NAUTIC_MILE * 60), TOLERANCE);
	}

	@Test
	public void testOneMinuteIsApproxiamtelyOneNauticalMile() {
		double lat1 = 60;
		double lon1 = 10;
		double latOneMinuteNorth = lat1 + (double) 1 / 60;
		GeodesicData g = Geodesic.WGS84.Inverse(lat1, lon1, latOneMinuteNorth, lon1);
		double oneDegreeInMeters = g.s12;
		assertThat(oneDegreeInMeters).isCloseTo(NAUTIC_MILE, TOLERANCE);
	}
}
