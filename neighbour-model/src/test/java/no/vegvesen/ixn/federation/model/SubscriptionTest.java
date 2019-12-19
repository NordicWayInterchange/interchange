package no.vegvesen.ixn.federation.model;

import  com.google.common.collect.Sets;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionTest {

	@Test
	public void getQuadTreeTilesSelector() {
		Subscription subscription = new Subscription();
		subscription.setQuadTreeTiles(Sets.newHashSet("bcde", "cdef"));
		assertThat(subscription.quadTreeTilesSelector()).isEqualTo("quadTree like '%,bcde%' or quadTree like '%,cdef%'");
	}

	@Test
	public void getSelectorWithQuadTreeBothSelectorAndQuadDefined() {
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		subscription.setQuadTreeTiles(Sets.newHashSet("bcde", "cdef"));
		assertThat(subscription.getSelectorWithQuadTree()).isEqualTo("(originatingCountry = 'NO') and (quadTree like '%,bcde%' or quadTree like '%,cdef%')");
	}

	@Test
	public void getSelectorWithQuadTreeOnlyQuadDefined() {
		Subscription subscription = new Subscription();
		subscription.setQuadTreeTiles(Sets.newHashSet("bcde", "cdef"));
		assertThat(subscription.getSelectorWithQuadTree()).isEqualTo("quadTree like '%,bcde%' or quadTree like '%,cdef%'");
	}

	@Test
	public void getSelectorWithQuadTreeOnlySelectorDefined() {
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		assertThat(subscription.getSelectorWithQuadTree()).isEqualTo("originatingCountry = 'NO'");
	}

	@Test
	public void getSelectorWithQuadTreeNeitherSelectorNorQuadDefinedReturnsNull() {
		Subscription subscription = new Subscription();
		assertThat(subscription.getSelectorWithQuadTree()).isNull();
	}

}