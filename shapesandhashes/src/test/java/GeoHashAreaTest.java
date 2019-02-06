import ch.hsr.geohash.GeoHash;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GeoHashAreaTest {

	@Test
	public void getNeigboursOfTwoHorisontallyAdjacentHashesIsTenHashes() {
		List<GeoHash> twoAdjacentHashes = Arrays.asList(GeoHash.fromGeohashString("u4xb"), GeoHash.fromGeohashString("u680"));
		GeoHashArea area = new GeoHashArea(new HashSet<>(twoAdjacentHashes));
		Set<GeoHash> neigbours = area.getNeigbours();
		assertThat(neigbours).hasSize(10);
	}

	@Test
	public void getNeigboursOfTwoDiagonallyAdjacentHashesIsTwelveHashes() {
		List<GeoHash> twoAdjacentHashes = Arrays.asList(GeoHash.fromGeohashString("u4wy"), GeoHash.fromGeohashString("u4xp"));
		GeoHashArea area = new GeoHashArea(new HashSet<>(twoAdjacentHashes));
		Set<GeoHash> neigbours = area.getNeigbours();
		assertThat(neigbours).hasSize(12);
	}

	@Test
	public void getNeigboursOfUnevenSizeHorisontallyAdjacentHashesIsEightHashes() {
		List<GeoHash> twoAdjacentHashes = Arrays.asList(GeoHash.fromGeohashString("u68p"), GeoHash.fromGeohashString("u4x"));
		GeoHashArea area = new GeoHashArea(twoAdjacentHashes);
		Set<GeoHash> neigbours = area.getNeigbours();
		assertThat(neigbours).hasSize(8);
	}

	@Test
	public void sameAreaIntersects() {
		GeoHashArea first = new GeoHashArea(Arrays.asList(GeoHash.fromGeohashString("u705"), GeoHash.fromGeohashString("u70h")));
		assertThat(first.intersects(first)).isTrue();
	}

	@Test
	public void otherAreaWithOneCommonHashIntersects() {
		GeoHashArea first = new GeoHashArea(Arrays.asList(GeoHash.fromGeohashString("u705"), GeoHash.fromGeohashString("u70h")));
		GeoHashArea other = new GeoHashArea(Arrays.asList(GeoHash.fromGeohashString("u705"), GeoHash.fromGeohashString("u70x")));
		assertThat(first.intersects(other)).isTrue();
	}

	@Test
	public void otherAreaWithNoCommonHashDoesNotIntersect() {
		GeoHashArea first = new GeoHashArea(Arrays.asList(GeoHash.fromGeohashString("u705"), GeoHash.fromGeohashString("u70h")));
		GeoHashArea other = new GeoHashArea(Arrays.asList(GeoHash.fromGeohashString("u70z"), GeoHash.fromGeohashString("u70x")));
		assertThat(first.intersects(other)).isFalse();
	}

	@Test
	public void otherAreaWithOneSubHashIntersect() {
		GeoHashArea first = new GeoHashArea(Arrays.asList(GeoHash.fromGeohashString("u705"), GeoHash.fromGeohashString("u70h")));
		GeoHashArea other = new GeoHashArea(Arrays.asList(GeoHash.fromGeohashString("u705f"), GeoHash.fromGeohashString("u70h4")));
		assertThat(first.intersects(other)).isTrue();
		assertThat(other.intersects(first)).isTrue();
	}
}