package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
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
		DenmApplication denm_a_b_causeCode_1_2 = new DenmApplication("publ-id-1", "pub-123", "NO", null, QUAD_TREE_0121_0122, Collections.singleton(6));
		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(denm_a_b_causeCode_1_2);
		capability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(capability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelector() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", null, QUAD_TREE_0121_0122, "SituationPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesDoesNotMatchDatexSelectorOutsideQuadTree() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", null, QUAD_TREE_0121_0122, "SituationPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,12234%'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTree() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "", "NO", null, QUAD_TREE_0121_0122, "SituationBublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012233%'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeLongerInFilter() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", null, QUAD_TREE_0121_0122, "SituationPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeAndPublicationType() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", null, QUAD_TREE_0121_0122, "MeasuredDataPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%' and publicationType = 'MeasuredDataPublication'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeAndOtherPublicationTypeDoesNotMatch() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-213", "NO", null, QUAD_TREE_0121_0122, "MeasuredDataPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%' and publicationType = 'Obstruction'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorOutsideQuadTreeLongerInFilter() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", null, QUAD_TREE_0121_0122,"Obstruction");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,121000%'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeWithExtraWhitespace() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", null, QUAD_TREE_0121_0122, "Obstruction");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		datexCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED," \n\t\n\n messageType = 'DATEX2' and \tquadTree \r\n\t\n\n like \t\t '%,01210%'\r\n", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	public void testDenmCapability() {
		Set<String> quadTreeTiles = new HashSet<>();
		quadTreeTiles.add("12004");
		DenmApplication application = new DenmApplication("NO-123", "pub-123", "NO", "DENM:1.2.2", quadTreeTiles, Collections.singleton(6));

		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(application);
		capability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);

		LocalSubscription subscription = new LocalSubscription(
				52,
				LocalSubscriptionStatus.CREATED,
				"((publisherId = 'NO-123') AND (quadTree like '%,12004%') AND (messageType = 'DENM') AND (causeCode = '6') AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'NO')) AND (originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6')",
				""
		);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(capability),
				Sets.newLinkedHashSet(subscription), ""
		);
		assertThat(commonInterest).hasSize(1);
	}

	@Test
	public void matchIviSelectorWithQuadTree() {
		IvimApplication application = new IvimApplication("NO-12345", "pub-2131", "NO", "IVI:1.0", Sets.newHashSet(Collections.singleton("12004")));
		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(application);
		capability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);

		String consumerCommonName = "";
		LocalSubscription localSubscription = new LocalSubscription("originatingCountry = 'NO' and messageType = 'IVIM' and protocolVersion = 'IVI:1.0' and quadTree like '%,12004%' and iviType like '%,6,%'",consumerCommonName);
		CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newHashSet(Collections.singleton(capability)), Sets.newHashSet(Collections.singleton(localSubscription)), consumerCommonName);
	}

	@Test
	public void matchSpatemSelectorWithQuadTree() {
		SpatemApplication application = new SpatemApplication(
				"NO-12345",
				"pub-123",
				"NO",
				"SPATEM:1.0",
				Sets.newHashSet(Collections.singleton("12003"))
		);
		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(application);
		capability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		String consumerCommonName = "";
		LocalSubscription localSubscription = new LocalSubscription("originatingCountry = 'NO' and messageType = 'SPATEM' and protocolVersion = 'SPATEM:1.0' and quadTree like '%,12003%' and id like '%,2,%' or id like '%,3,%'",consumerCommonName);
		System.out.println(localSubscription.getSelector());
		Set<LocalSubscription> localSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newHashSet(Collections.singleton(capability)), Collections.singleton(localSubscription), consumerCommonName);
		assertThat(localSubscriptions).isNotEmpty();
	}

	@Test
	void camCapabilitiesMatchCamSelectorInsideQuadTreeAndStationType() {
		CamApplication camApplication = new CamApplication("publ-id-1", "pub-1", "NO", null, QUAD_TREE_0121_0122);
		CapabilitySplit camCapability = new CapabilitySplit();
		camCapability.setApplication(camApplication);
		camCapability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(camCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'CAM' and quadTree like '%,012100%' and stationType = 'cyclist'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	@Disabled("At the moment, it's not possible to filter on optional fields that are not a part of the set of properties")
	void mathcCapabilityWithSelectorOfOnlyOptionalField() {
		SpatemApplication application = new SpatemApplication(
				"NO-12345",
				"pub-1",
				"NO",
				"SPATEM:1.0",
				Sets.newHashSet(Collections.singleton("12003"))
		);
		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(application);
		capability.getMetadata().setRedirectPolicy(RedirectStatus.OPTIONAL);
		String consumerCommonName = "";
		LocalSubscription localSubscription = new LocalSubscription("name = 'fish'",consumerCommonName);
		System.out.println(localSubscription.getSelector());
		Set<LocalSubscription> localSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newHashSet(Collections.singleton(capability)), Collections.singleton(localSubscription), consumerCommonName);
		assertThat(localSubscriptions).isNotEmpty();
	}

}