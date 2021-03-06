package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;

public class Datex2DataTypeApi extends DataTypeApi{

	public static final String DATEX_2 = "DATEX2";
	private String publicationType;
	private Set<String> publicationSubType = new HashSet<>();

	public Datex2DataTypeApi() {
		this.setMessageType(DATEX_2);
	}

	public Datex2DataTypeApi(String publisherId, String originatingCountry, String protocolVersion,  Set<String> quadTree, String publicationType, Set<String> publicationSubType) {
		super(DATEX_2, publisherId, originatingCountry, protocolVersion,  quadTree);
		this.publicationType = publicationType;
		if (publicationSubType != null) {
			this.publicationSubType.addAll(publicationSubType);
		}
	}

	public Datex2DataTypeApi(String originatingCountry) {
		super(DATEX_2,  originatingCountry, null, null, Collections.emptySet());
	}

	public String getPublicationType() {
		return this.publicationType;
	}

	public void setPublicationType(String publicationType) {
		this.publicationType = publicationType;
	}

	public Set<String> getPublicationSubType() {
		return publicationSubType;
	}

	public void setPublicationSubType(Collection<String> publicationSubType) {
		this.publicationSubType.clear();
		if (publicationSubType != null) {
			this.publicationSubType.addAll(publicationSubType);
		}
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.PUBLICATION_TYPE, this.getPublicationType());
		putValue(values, MessageProperty.PUBLICATION_SUB_TYPE, this.getPublicationSubType());
		return values;
	}

	@Override
	public String toString() {
		return 	"Datex2DataTypeApi{" +
				super.baseToString() +
				", publicationType='" + publicationType + '\'' +
				", publicationSubType=" + publicationSubType +
				'}';
	}
}
