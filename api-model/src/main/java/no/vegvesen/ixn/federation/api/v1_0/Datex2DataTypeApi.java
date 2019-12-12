package no.vegvesen.ixn.federation.api.v1_0;

public class Datex2DataTypeApi extends DataTypeApi implements Datex2DataTypeI {

	@SuppressWarnings("WeakerAccess")
	public static final String DATEX_2 = "DATEX2";
	private String publicationType;

	public Datex2DataTypeApi() {
	}

	public Datex2DataTypeApi(String where, String publicationType) {
		super(DATEX_2, where);
		this.publicationType = publicationType;
	}

	public Datex2DataTypeApi(String where) {
		super(DATEX_2, where);
	}

	@Override
	public String getPublicationType() {
		return this.publicationType;
	}

	@Override
	public void setPublicationType(String publicationType) {
		this.publicationType = publicationType;
	}
}
