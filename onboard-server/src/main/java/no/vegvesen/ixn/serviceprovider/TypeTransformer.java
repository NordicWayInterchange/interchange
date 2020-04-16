package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.transformer.DataTypeTransformer;
import no.vegvesen.ixn.serviceprovider.model.LocalDataType;
import no.vegvesen.ixn.serviceprovider.model.LocalDataTypeList;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TypeTransformer {

    private DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();

    public TypeTransformer(DataTypeTransformer dataTypeTransformer) {
        this.dataTypeTransformer = dataTypeTransformer;
    }

	public LocalDataTypeList transformToDataTypeIdList(Set<DataType> dataTypes) {
		List<LocalDataType> idDataTypes = new LinkedList<>();
		for (DataType dataType : dataTypes) {
			idDataTypes.add(transformToDataTypeApiId(dataType));
		}
		return new LocalDataTypeList(idDataTypes);
	}

    public LocalDataType transformToDataTypeApiId(DataType dataType) {
        return new LocalDataType(dataType.getData_id(), dataTypeTransformer.dataTypeToApi(dataType));
    }
}
