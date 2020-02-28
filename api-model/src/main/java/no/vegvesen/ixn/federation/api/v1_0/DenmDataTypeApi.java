package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.Map;
import java.util.Set;

public class DenmDataTypeApi extends EtsiDataTypeApi{
	public static final String DENM = "DENM";

	private String causeCode;
	private String subCauseCode;

	public DenmDataTypeApi() {
		this.setMessageType(DENM);
	}

	public DenmDataTypeApi(String publisherId, String publisherName, String originatingCountry,
						   String protocolVersion, String contentType, Set<String> quadTree,
						   String serviceType, String causeCode, String subCauseCode) {
		super(DENM, publisherId, publisherName, originatingCountry, protocolVersion, contentType, quadTree, serviceType);
		this.causeCode = causeCode;
		this.subCauseCode = subCauseCode;
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.CAUSE_CODE, this.getCauseCode());
		putValue(values, MessageProperty.SUB_CAUSE_CODE, this.getSubCauseCode());
		return values;
	}

	@SuppressWarnings("WeakerAccess")
	public String getCauseCode() {
		return causeCode;
	}

	public void setCauseCode(String causeCode) {
		this.causeCode = causeCode;
	}

	@SuppressWarnings("WeakerAccess")
	public String getSubCauseCode() {
		return subCauseCode;
	}

	public void setSubCauseCode(String subCauseCode) {
		this.subCauseCode = subCauseCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DenmDataTypeApi)) return false;
		if (!super.equals(o)) return false;

		DenmDataTypeApi that = (DenmDataTypeApi) o;

		if (causeCode != null ? !causeCode.equals(that.causeCode) : that.causeCode != null) return false;
		return subCauseCode != null ? subCauseCode.equals(that.subCauseCode) : that.subCauseCode == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (causeCode != null ? causeCode.hashCode() : 0);
		result = 31 * result + (subCauseCode != null ? subCauseCode.hashCode() : 0);
		return result;
	}
}
