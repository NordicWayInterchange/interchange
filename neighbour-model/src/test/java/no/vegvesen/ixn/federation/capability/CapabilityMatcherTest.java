package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityMatcherTest {

	private static final LinkedHashSet<String> QUAD_TREE_0121_0122 = Sets.newLinkedHashSet("0121", "0122");
	private static final LinkedHashSet<String> PUBL_TYPES_SITUATION_MEASURED_DATA = Sets.newLinkedHashSet("SituationPublication", "MeasuredDataPublication");

	@Test
	void denmCapabilitiesDoesNotMatchDatexSelector() {
		DenmCapability denm_a_b_causeCode_1_2 = new DenmCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(denm_a_b_causeCode_1_2),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'")));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelector() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'")));
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesDoesNotMatchDatexSelectorOutsideQuadTree() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,12234%'")));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTree() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012233%'")));
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeLongerInFilter() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%'")));
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeAndPublicationType() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%' and publicationType = 'MeasuredDataPublication'")));
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeAndOtherPublicationTypeDoesNotMatch() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%' and publicationType = 'Obstruction'")));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorOutsideQuadTreeLongerInFilter() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,121000%'")));
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeWithExtraWhitespace() {
		DatexCapability datexCapability = new DatexCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, PUBL_TYPES_SITUATION_MEASURED_DATA);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED," \n\t\n\n messageType = 'DATEX2' and \tquadTree \r\n\t\n\n like \t\t '%,01210%'\r\n")));
		assertThat(commonInterest).isNotEmpty();
	}

}