package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.properties.MessageProperty;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.HashMap;

import static no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi.DATEX_2;
import static no.vegvesen.ixn.federation.api.v1_0.IviDataTypeApi.IVI;
import static org.assertj.core.api.Assertions.assertThat;

class SelectorSubscriptionTest {

	@Test
	void capabilities_12_56_46_datexNoMatches__SelectorSubscription_12() {
		DataType datexNo12002 = getDataType(DATEX_2, "NO", ",12,56,46");
		SelectorSubscription ssDatexNo12002 = new SelectorSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'", Sets.newSet("12"));
		assertThat(ssDatexNo12002.matches(datexNo12002)).isTrue();
	}

	@Test
	void datexNoQuad12002MatchesEqualSelector() {
		DataType datexNo12002 = getDataType(DATEX_2, "NO", ",12002");
		SelectorSubscription ssDatexNo12002 = new SelectorSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'", Sets.newSet("12002"));
		assertThat(ssDatexNo12002.matches(datexNo12002)).isTrue();
	}

	@Test
	void datexNoMorePreciseSubscriptionQuadMatchesEqualSelector() {
		DataType datexNo12002 = getDataType(DATEX_2, "NO", ",12002");
		SelectorSubscription ssDatexNo12002 = new SelectorSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'", Sets.newSet("120021234"));
		assertThat(ssDatexNo12002.matches(datexNo12002)).isTrue();
	}

	@Test
	void datexNoMorePreciseCapabilityQuadMatchesEqualSelector() {
		DataType datexNo12002 = getDataType(DATEX_2, "NO", ",120021234");
		SelectorSubscription ssDatexNo12002 = new SelectorSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'", Sets.newSet("12002"));
		assertThat(ssDatexNo12002.matches(datexNo12002)).isTrue();
	}

	@Test
	void datexNoDifferentQuad123DoesNotMatch() {
		DataType datexNo12002 = getDataType(DATEX_2, "NO", ",12002");
		SelectorSubscription ssDatexNo12002 = new SelectorSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'", Sets.newSet("12003"));
		assertThat(ssDatexNo12002.matches(datexNo12002)).isFalse();
	}

	@Test
	void differentMessageTypeSameQuadAndOriginatingCountryDoesNotMatch() {
		DataType datexNo12002 = getDataType(DATEX_2, "NO", ",12002");
		SelectorSubscription ssDatexNo12002 = new SelectorSubscription("messageType = 'DENM' AND originatingCountry = 'NO'", Sets.newSet("12002"));
		assertThat(ssDatexNo12002.matches(datexNo12002)).isFalse();
	}

	@Test
	void originatingCountryNotEqualDoesNotMatch() {
		DataType no = getDataType(DATEX_2, "NO", "1234");
		SelectorSubscription seSelector = new SelectorSubscription("messageType = 'DATEX2' and originatingCountry = 'SE'", Sets.newSet("1234"));
		assertThat(seSelector.matches(no)).isFalse();
	}

	@Test
	void iviTypeMatches() {
		HashMap<String, String> iviNo1234 = getDataTypeHeaderValues(IVI, "NO", "1234");
		iviNo1234.put(MessageProperty.IVI_TYPE.getName(), "MY-IVI-TYPE");
		DataType iviDT = new DataType(iviNo1234);
		SelectorSubscription iviMyIviTypeNo123456 = new SelectorSubscription("messageType = 'IVI' and originatingCountry = 'NO' and iviType = 'MY-IVI-TYPE'", Sets.newSet("123456"));
		assertThat(iviMyIviTypeNo123456.matches(iviDT)).isTrue();
	}

	@Test
	void iviTypeMatchesLike() {
		HashMap<String, String> iviNo1234 = getDataTypeHeaderValues(IVI, "NO", "1234");
		iviNo1234.put(MessageProperty.IVI_TYPE.getName(), "MY-IVI-TYPE");
		DataType iviDT = new DataType(iviNo1234);
		SelectorSubscription iviMyIviTypeNo123456 = new SelectorSubscription("messageType = 'IVI' and originatingCountry = 'NO' and iviType like 'MY-IVI%'", Sets.newSet("123456"));
		assertThat(iviMyIviTypeNo123456.matches(iviDT)).isTrue();
	}

	@Test
	void iviTypeMatchesIn() {
		HashMap<String, String> iviNo1234 = getDataTypeHeaderValues(IVI, "NO", "1234");
		iviNo1234.put(MessageProperty.IVI_TYPE.getName(), "MY-IVI-TYPE");
		DataType iviDT = new DataType(iviNo1234);
		SelectorSubscription iviMyIviTypeNo123456 = new SelectorSubscription("messageType = 'IVI' and originatingCountry = 'NO' and iviType in ('MY-IVI-TYPE', 'OTHER-IVI-TYPE')", Sets.newSet("123456"));
		assertThat(iviMyIviTypeNo123456.matches(iviDT)).isTrue();
	}

	@Test
	void iviTypeMatchesNotIn() {
		HashMap<String, String> iviNo1234 = getDataTypeHeaderValues(IVI, "NO", "1234");
		iviNo1234.put(MessageProperty.IVI_TYPE.getName(), "MY-IVI-TYPE");
		DataType iviDT = new DataType(iviNo1234);
		SelectorSubscription iviMyIviTypeNo123456 = new SelectorSubscription("messageType = 'IVI' and originatingCountry = 'NO' and iviType in ('SECOND-IVI-TYPE', 'OTHER-IVI-TYPE')", Sets.newSet("123456"));
		assertThat(iviMyIviTypeNo123456.matches(iviDT)).isFalse();
	}

	/*
--messageType
--originatingCountry
--quadTree
--iviType
publisherId
publicationSubType
pictogramCategoryCode
publicationType
relation
causeCode
subCauseCode
protocolVersion
serviceType
publisherName
contentType
	 */


	private DataType getDataType(String messageType, String originatingCountry, String quadTree) {
		HashMap<String, String> datexHeaders = getDataTypeHeaderValues(messageType, originatingCountry, quadTree);
		return new DataType(datexHeaders);
	}

	private HashMap<String, String> getDataTypeHeaderValues(String messageType, String originatingCountry, String quadTree) {
		HashMap<String, String> datexHeaders = new HashMap<>();
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), messageType);
		datexHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		datexHeaders.put(MessageProperty.QUAD_TREE.getName(), quadTree);
		return datexHeaders;
	}

}