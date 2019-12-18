package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;

public class Datex2DataTypeApi extends DataTypeApi implements Datex2DataTypeI {

	public static final String DATEX_2 = "DATEX2";
	private String publicationType;
	private Set<String> publicationSubType = new HashSet<>();

	public Datex2DataTypeApi() {
	}

	public Datex2DataTypeApi(String originatingCountry, Set<String> quadTree, String publicationType, Set<String> publicationSubType) {
		super(DATEX_2, originatingCountry, quadTree);
		this.publicationType = publicationType;
		if (publicationSubType != null) {
			this.publicationSubType.addAll(publicationSubType);
		}
	}

	public Datex2DataTypeApi(String originatingCountry) {
		super(DATEX_2, originatingCountry, Collections.emptySet());
	}

	@Override
	public String getPublicationType() {
		return this.publicationType;
	}

	@Override
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
}
