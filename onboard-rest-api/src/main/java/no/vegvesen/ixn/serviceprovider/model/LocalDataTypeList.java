package no.vegvesen.ixn.serviceprovider.model;

import java.util.LinkedList;
import java.util.List;

public class LocalDataTypeList {
	List<LocalDataType> dataTypes = new LinkedList<>();

	public LocalDataTypeList() {
	}

	public LocalDataTypeList(List<LocalDataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

	public List<LocalDataType> getDataTypes() {
		return dataTypes;
	}

	public void setDataTypes(List<LocalDataType> dataTypes) {
		this.dataTypes = dataTypes;
	}
}
