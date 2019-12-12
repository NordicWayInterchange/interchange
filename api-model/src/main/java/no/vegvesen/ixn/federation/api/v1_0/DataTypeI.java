package no.vegvesen.ixn.federation.api.v1_0;

public interface DataTypeI {
	String getOriginatingCountry();

	void setOriginatingCountry(String where);

	String getHow();

	void setHow(String how);
}
