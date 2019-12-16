package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.Map;

public class Datex2DataTypeApi extends DataTypeApi implements Datex2DataTypeI {

	public static final String DATEX_2 = "DATEX2";
	private String publicationType;
	private String[] publicationSubType;

	public Datex2DataTypeApi() {
	}

	public Datex2DataTypeApi(String where, String publicationType, String[] publicationSubType) {
		super(DATEX_2, where);
		this.publicationType = publicationType;
		this.publicationSubType = publicationSubType;
	}

	public Datex2DataTypeApi(String originatingCountry) {
		super(DATEX_2, originatingCountry);
	}

	@Override
	public String getPublicationType() {
		return this.publicationType;
	}

	@Override
	public void setPublicationType(String publicationType) {
		this.publicationType = publicationType;
	}

	public String[] getPublicationSubType() {
		return publicationSubType;
	}

	public void setPublicationSubType(String[] publicationSubType) {
		this.publicationSubType = publicationSubType;
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.PUBLICATION_TYPE, this.getPublicationType());
		putValue(values, MessageProperty.PUBLICATION_SUB_TYPE, this.arrayToDelimitedString(this.getPublicationSubType()));
		return values;
	}
}
