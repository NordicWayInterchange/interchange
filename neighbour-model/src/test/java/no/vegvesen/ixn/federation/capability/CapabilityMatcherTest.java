package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityMatcherTest {

	@Test
	void denmCapabilitiesDoesNotMatchDatexSelector() {
		DenmCapabilityApi denm_a_b_causeCode_1_2 = new DenmCapabilityApi("publ-id-1", "NO", null, org.assertj.core.util.Sets.newLinkedHashSet("a", "b"), org.assertj.core.util.Sets.newLinkedHashSet("1", "2"));
		JMSSelectorFilter datex2 = JMSSelectorFilterFactory.get("messageType = 'DATEX2'");
		Set<JMSSelectorFilter> commonInterest = CapabilityMatcher.calculateCommonInterest(Sets.newLinkedHashSet(denm_a_b_causeCode_1_2), Sets.newLinkedHashSet(datex2));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelector() {
		DatexCapabilityApi datexCapabilityApi = new DatexCapabilityApi("publ-id-1", "NO", null, Sets.newLinkedHashSet("a", "b"), Sets.newLinkedHashSet("1", "2"));
		JMSSelectorFilter datex2Selector = JMSSelectorFilterFactory.get("messageType = 'DATEX2'");
		Set<JMSSelectorFilter> commonInterest = CapabilityMatcher.calculateCommonInterest(Sets.newLinkedHashSet(datexCapabilityApi), Sets.newLinkedHashSet(datex2Selector));
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesDoesNotMatchDatexSelectorOutsideQuadTree() {
		DatexCapabilityApi datexCapabilityApi = new DatexCapabilityApi("publ-id-1", "NO", null, Sets.newLinkedHashSet("a", "b"), Sets.newLinkedHashSet("1", "2"));
		JMSSelectorFilter datex2Selector = JMSSelectorFilterFactory.get("messageType = 'DATEX2' and quadTree like 'c%'");
		Set<JMSSelectorFilter> commonInterest = CapabilityMatcher.calculateCommonInterest(Sets.newLinkedHashSet(datexCapabilityApi), Sets.newLinkedHashSet(datex2Selector));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTree() {
		DatexCapabilityApi datexCapabilityApi = new DatexCapabilityApi("publ-id-1", "NO", null, Sets.newLinkedHashSet("a", "b"), Sets.newLinkedHashSet("1", "2"));
		JMSSelectorFilter datex2Selector = JMSSelectorFilterFactory.get("messageType = 'DATEX2' and quadTree like 'b%'");
		Set<JMSSelectorFilter> commonInterest = CapabilityMatcher.calculateCommonInterest(Sets.newLinkedHashSet(datexCapabilityApi), Sets.newLinkedHashSet(datex2Selector));
		assertThat(commonInterest).isNotEmpty();
	}

}