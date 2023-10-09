package no.vegvesen.ixn.federation.capability;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CapabilityMatcherTest {

	private static final LinkedHashSet<String> QUAD_TREE_0121_0122 = Sets.newLinkedHashSet("0121", "0122");

	@Test
	void denmCapabilitiesDoesNotMatchDatexSelector() {
		DenmApplication denm_a_b_causeCode_1_2 = new DenmApplication("publ-id-1", "pub-123", "NO", "1.0", QUAD_TREE_0121_0122, Collections.singleton(6));
		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(denm_a_b_causeCode_1_2);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		capability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(capability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelector() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", "1.0", QUAD_TREE_0121_0122, "SituationPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesDoesNotMatchDatexSelectorOutsideQuadTree() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", "1.0", QUAD_TREE_0121_0122, "SituationPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,12234%'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTree() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "", "NO", "1.0", QUAD_TREE_0121_0122, "SituationBublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012233%'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeLongerInFilter() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", "1.0", QUAD_TREE_0121_0122, "SituationPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeAndPublicationType() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", "1.0", QUAD_TREE_0121_0122, "MeasuredDataPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%' and publicationType = 'MeasuredDataPublication'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeAndOtherPublicationTypeDoesNotMatch() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-213", "NO", "1.0", QUAD_TREE_0121_0122, "MeasuredDataPublication");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,012100%' and publicationType = 'Obstruction'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorOutsideQuadTreeLongerInFilter() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", "1.0", QUAD_TREE_0121_0122,"Obstruction");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(datexCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' and quadTree like '%,121000%'", "")), "");
		assertThat(commonInterest).isEmpty();
	}

	@Test
	void datexCapabilitiesMatchDatexSelectorInsideQuadTreeWithExtraWhitespace() {
		DatexApplication datexApplication = new DatexApplication("publ-id-1", "pub-123", "NO", "1.0", QUAD_TREE_0121_0122, "Obstruction");
		CapabilitySplit datexCapability = new CapabilitySplit();
		datexCapability.setApplication(datexApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		datexCapability.setMetadata(meta);
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
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		capability.setMetadata(meta);

		LocalSubscription subscription = new LocalSubscription(
				52,
				LocalSubscriptionStatus.CREATED,
				"((publisherId = 'NO-123') AND (quadTree like '%,12004%') AND (messageType = 'DENM') AND (causeCode = 6) AND (protocolVersion = 'DENM:1.2.2') AND (originatingCountry = 'NO')) AND (originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6)",
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
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		capability.setMetadata(meta);

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
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		capability.setMetadata(meta);
		String consumerCommonName = "";
		LocalSubscription localSubscription = new LocalSubscription("originatingCountry = 'NO' and messageType = 'SPATEM' and protocolVersion = 'SPATEM:1.0' and quadTree like '%,12003%' and id = 2 or id = 3",consumerCommonName);
		System.out.println(localSubscription.getSelector());
		Set<LocalSubscription> localSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newHashSet(Collections.singleton(capability)), Collections.singleton(localSubscription), consumerCommonName);
		assertThat(localSubscriptions).isNotEmpty();
	}

	@Test
	void camCapabilitiesMatchCamSelectorInsideQuadTreeAndStationType() {
		CamApplication camApplication = new CamApplication("publ-id-1", "pub-1", "NO", "1.0", QUAD_TREE_0121_0122);
		CapabilitySplit camCapability = new CapabilitySplit();
		camCapability.setApplication(camApplication);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		camCapability.setMetadata(meta);
		Set<LocalSubscription> commonInterest = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(
				Sets.newLinkedHashSet(camCapability),
				Sets.newLinkedHashSet(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'CAM' and originatingCountry = 'NO' and protocolVersion = '1.0' and quadTree like '%,0121%' and stationType = 'cyclist'", "")), "");
		assertThat(commonInterest).isNotEmpty();
	}

	@Test
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
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		capability.setMetadata(meta);
		String consumerCommonName = "";
		LocalSubscription localSubscription = new LocalSubscription("name = 'fish'",consumerCommonName);
		System.out.println(localSubscription.getSelector());
		Set<LocalSubscription> localSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(Sets.newHashSet(Collections.singleton(capability)), Collections.singleton(localSubscription), consumerCommonName);
		assertThat(localSubscriptions).isNotEmpty();
	}

	@Test
	void newMatchDenmCapabilityWithSelector() throws JsonProcessingException {
		DenmApplication denm_a_b_causeCode_1_2 = new DenmApplication("publ-id-1", "pub-123", "NO", "DENM:1.2.2", QUAD_TREE_0121_0122, Collections.singleton(6));
		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(denm_a_b_causeCode_1_2);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		capability.setMetadata(meta);

		String selector = "originatingCountry = 'NO' AND causeCodes = 6 OR causeCodes = 5";

		assertThat(CapabilityMatcher.matchCapabilityToSelector(capability, selector)).isTrue();
	}

	@Test
	void matchEmptyCauseCodeListWithSelectorContainingCauseCode() {
		DenmApplication denm_a_b_causeCode_1_2 = new DenmApplication("publ-id-1", "pub-123", "NO", "DENM:1.2.2", QUAD_TREE_0121_0122, Collections.emptySet());
		CapabilitySplit capability = new CapabilitySplit();
		capability.setApplication(denm_a_b_causeCode_1_2);
		Metadata meta = new Metadata(RedirectStatus.OPTIONAL);
		capability.setMetadata(meta);

		String selector = "originatingCountry = 'NO' AND causeCodes = 5";

		assertThat(CapabilityMatcher.matchCapabilityToSelector(capability, selector)).isTrue();
	}
}