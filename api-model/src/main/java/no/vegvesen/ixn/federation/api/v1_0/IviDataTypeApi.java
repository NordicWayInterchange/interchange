package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.Map;
import java.util.Set;

public class IviDataTypeApi extends EtsiDataTypeApi{
	public static final String IVI = "IVI";

	private Integer iviType;
	private Set<Integer> pictogramCategoryCodes;

	public IviDataTypeApi() {
		this.setMessageType(IVI);
	}

	public IviDataTypeApi(String publisherId, String publisherName, String originatingCountry,
						  String protocolVersion, String contentType, Set<String> quadTree,
						  String serviceType, Integer iviType, Set<Integer> pictogramCategoryCodes) {
		super(IVI, publisherId, publisherName, originatingCountry, protocolVersion, contentType, quadTree, serviceType);
		this.iviType = iviType;
		this.pictogramCategoryCodes = pictogramCategoryCodes;
	}


	public Integer getIviType() {
		return iviType;
	}

	public void setIviType(Integer iviType) {
		this.iviType = iviType;
	}

	@SuppressWarnings("WeakerAccess")
	public Set<Integer> getPictogramCategoryCodes() {
		return pictogramCategoryCodes;
	}

	public void setPictogramCategoryCodes(Set<Integer> pictogramCategoryCodes) {
		this.pictogramCategoryCodes = pictogramCategoryCodes;
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.IVI_TYPE, this.getIviType());
		putIntegerValue(values, MessageProperty.PICTOGRAM_CATEGORY_CODE, this.getPictogramCategoryCodes());
		return values;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IviDataTypeApi)) return false;
		if (!super.equals(o)) return false;

		IviDataTypeApi that = (IviDataTypeApi) o;

		if (iviType != null ? !iviType.equals(that.iviType) : that.iviType != null) return false;
		return pictogramCategoryCodes != null ? pictogramCategoryCodes.equals(that.pictogramCategoryCodes) : that.pictogramCategoryCodes == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (iviType != null ? iviType.hashCode() : 0);
		result = 31 * result + (pictogramCategoryCodes != null ? pictogramCategoryCodes.hashCode() : 0);
		return result;
	}
}
