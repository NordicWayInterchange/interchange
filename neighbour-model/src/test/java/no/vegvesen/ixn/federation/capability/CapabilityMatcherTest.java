package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityMatcherTest {

	@Test
	void denmCapabilitiesDoesNotMatchDatexSelector() {
		DenmCapability denm_a_b_causeCode_1_2 = new DenmCapability("publ-id-1", "NO", null, org.assertj.core.util.Sets.newLinkedHashSet("a", "b"), org.assertj.core.util.Sets.newLinkedHashSet("1", "2"));
		Set<String> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newLinkedHashSet(denm_a_b_causeCode_1_2), Sets.newLinkedHashSet("messageType = 'DATEX2'"));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelector() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, Sets.newLinkedHashSet("a", "b"), Sets.newLinkedHashSet("1", "2"));
		Set<String> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newLinkedHashSet(datexCapability), Sets.newLinkedHashSet("messageType = 'DATEX2'"));
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesDoesNotMatchDatexSelectorOutsideQuadTree() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, Sets.newLinkedHashSet("a", "b"), Sets.newLinkedHashSet("1", "2"));
		Set<String> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newLinkedHashSet(datexCapability), Sets.newLinkedHashSet("messageType = 'DATEX2' and quadTree like 'c%'"));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTree() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, Sets.newLinkedHashSet("a", "b"), Sets.newLinkedHashSet("1", "2"));
		Set<String> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newLinkedHashSet(datexCapability), Sets.newLinkedHashSet("messageType = 'DATEX2' and quadTree like 'b%'"));
		assertThat(commonInterest).isNotEmpty();
	}

}