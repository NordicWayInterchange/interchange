package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.Map;
import java.util.Set;

public class IvyDataTypeApi extends EtsiDataTypeApi{
	public static final String IVY = "IVY";

	private Integer ivyType;
	private Set<Integer> pictogramCategoryCodes;

	public IvyDataTypeApi() {
		this.setMessageType(IVY);
	}

	public IvyDataTypeApi(String publisherId, String publisherName, String originatingCountry,
						  String protocolVersion, String contentType, Set<String> quadTree,
						  String serviceType, Integer ivyType, Set<Integer> pictogramCategoryCodes) {
		super(IVY, publisherId, publisherName, originatingCountry, protocolVersion, contentType, quadTree, serviceType);
		this.ivyType = ivyType;
		this.pictogramCategoryCodes = pictogramCategoryCodes;
	}


	public Integer getIvyType() {
		return ivyType;
	}

	public void setIvyType(Integer ivyType) {
		this.ivyType = ivyType;
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
		putValue(values, MessageProperty.IVI_TYPE, this.getIvyType());
		putIntegerValue(values, MessageProperty.PICTOGRAM_CATEGORY_CODE, this.getPictogramCategoryCodes());
		return values;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IvyDataTypeApi)) return false;
		if (!super.equals(o)) return false;

		IvyDataTypeApi that = (IvyDataTypeApi) o;

		if (ivyType != null ? !ivyType.equals(that.ivyType) : that.ivyType != null) return false;
		return pictogramCategoryCodes != null ? pictogramCategoryCodes.equals(that.pictogramCategoryCodes) : that.pictogramCategoryCodes == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (ivyType != null ? ivyType.hashCode() : 0);
		result = 31 * result + (pictogramCategoryCodes != null ? pictogramCategoryCodes.hashCode() : 0);
		return result;
	}
}
