package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.*;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;

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

	@Test
	public void testDenmCapability() {
		Set<String> quadTreeTiles = new HashSet<>();
		quadTreeTiles.add("12001");
		quadTreeTiles.add("12003");
		DenmCapability capability = new DenmCapability(
				"SE-00001",
				"SE",
				"DENM:1.3.1",
				quadTreeTiles,
				Collections.singleton("42")
		);
		LocalSubscription subscription = new LocalSubscription(
				52,
				LocalSubscriptionStatus.CREATED,
				"originatingCountry = 'SE' and messageType = 'DENM' and quadTree like '%,12003%'",
				false,
				"kyrre"
		);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(capability),
				Sets.newLinkedHashSet(subscription)
		);
		assertThat(commonInterest).hasSize(1);
	}

	@Test
	@Disabled
	public void matchIviSelectorWithQuadTree() {
		IvimCapability capability = new IvimCapability(
				"NO-12345",
				"NO",
				"IVI:1.0",
				Sets.newHashSet(Collections.singleton("12004")),
				Sets.newHashSet(Arrays.asList("1", "2"))
		);
		LocalSubscription localSubscription = new LocalSubscription();
		localSubscription.setSelector("originatingCountry = 'NO' and messageType = 'IVIM' and protocolVersion = 'IVI:1.0' and quadTree like '%,12004%' and iviType like '%,2,%' ");
		System.out.println(localSubscription.getSelector());
		Set<LocalSubscription> localSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newHashSet(Collections.singleton(capability)), Collections.singleton(localSubscription));
		assertThat(localSubscriptions).isNotEmpty();
	}

	@Test
	@Disabled
	public void matchSpatemSelectorWithQuadTree() {
		SpatemCapability capability = new SpatemCapability(
				"NO-12345",
				"NO",
				"SPATEM:1.0",
				Sets.newHashSet(Collections.singleton("12003")),
				Sets.newHashSet(Arrays.asList("1", "2"))
		);
		LocalSubscription localSubscription = new LocalSubscription();
		localSubscription.setSelector("originatingCountry = 'NO' and messageType = 'SPATEM' and protocolVersion = 'SPATEM:1.0' and quadTree like '%,12003%' and id like '%,2,%' or id like '%,3,%'");
		System.out.println(localSubscription.getSelector());
		Set<LocalSubscription> localSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newHashSet(Collections.singleton(capability)), Collections.singleton(localSubscription));
		assertThat(localSubscriptions).isNotEmpty();
	}

	@Test
	@Disabled
	void camCapabilitiesMatchCamSelectorInsideQuadTreeAndStationType() {
		Set<String> statTypes = Sets.newLinkedHashSet("pedestrian", "cyclist", "moped");
		CamCapability camCapability = new CamCapability("publ-id-1", "NO", null, QUAD_TREE_0121_0122, statTypes);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(camCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'CAM' and quadTree like '%,012100%' and stationType = 'cyclist'")));
		assertThat(commonInterest).isNotEmpty();
	}

}