package no.vegvesen.ixn.serviceprovider.model;

import java.util.LinkedList;
import java.util.List;

public class DataTypeIdList {
	List<DataTypeApiId> dataTypes = new LinkedList<>();

	public DataTypeIdList() {
	}

	public DataTypeIdList(List<DataTypeApiId> dataTypes) {
		this.dataTypes = dataTypes;
	}

	public List<DataTypeApiId> getDataTypes() {
		return dataTypes;
	}

	public void setDataTypes(List<DataTypeApiId> dataTypes) {
		this.dataTypes = dataTypes;
	}
}
