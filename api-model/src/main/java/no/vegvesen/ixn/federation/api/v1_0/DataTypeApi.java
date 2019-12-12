package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "messageType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = Datex2DataTypeApi.class, name = Datex2DataTypeApi.DATEX_2),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTypeApi implements DataTypeI {

	private String messageType;
	private String originatingCountry;

	public DataTypeApi() {
	}

	public DataTypeApi(String messageType, String originatingCountry) {
		this.originatingCountry = originatingCountry;
		this.messageType = messageType;
	}

	@Override
	public String getOriginatingCountry() {
		return this.originatingCountry;
	}

	@Override
	public void setOriginatingCountry(String originatingCountry) {
		this.originatingCountry = originatingCountry;
	}

	@Override
	public String getMessageType() {
		return this.messageType;
	}

	@Override
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataTypeApi that = (DataTypeApi) o;

		if (messageType != null ? !messageType.equals(that.messageType) : that.messageType != null) return false;
		return originatingCountry != null ? originatingCountry.equals(that.originatingCountry) : that.originatingCountry == null;
	}

	@Override
	public int hashCode() {
		int result = messageType != null ? messageType.hashCode() : 0;
		result = 31 * result + (originatingCountry != null ? originatingCountry.hashCode() : 0);
		return result;
	}
}
