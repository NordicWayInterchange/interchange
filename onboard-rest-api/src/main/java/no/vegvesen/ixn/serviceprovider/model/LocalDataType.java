package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;

public class LocalDataType {
	Integer id;
	DataTypeApi dataType;

	public LocalDataType() {
	}

	public LocalDataType(Integer id, DataTypeApi dataType) {
		this.id = id;
		this.dataType = dataType;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DataTypeApi getDataType() {
		return dataType;
	}

	public void setDataType(DataTypeApi dataType) {
		this.dataType = dataType;
	}
}
