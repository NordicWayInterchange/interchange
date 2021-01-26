package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.Map;
import java.util.Set;

public abstract class EtsiDataTypeApi extends DataTypeApi{

	private String serviceType;

	EtsiDataTypeApi() {
	}

	EtsiDataTypeApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, String serviceType) {
		super(messageType, publisherId, originatingCountry, protocolVersion, quadTree);
		this.serviceType = serviceType;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.SERVICE_TYPE, this.getServiceType());
		return values;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EtsiDataTypeApi)) return false;
		if (!super.equals(o)) return false;

		EtsiDataTypeApi that = (EtsiDataTypeApi) o;

		return serviceType != null ? serviceType.equals(that.serviceType) : that.serviceType == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
		return result;
	}

	public String baseToString() {
		return 	super.baseToString() +
				", serviceType='" + serviceType + '\'';
	}
}
