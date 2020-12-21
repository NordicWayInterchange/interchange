package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.Map;
import java.util.Set;

public class IviDataTypeApi extends EtsiDataTypeApi{
	public static final String IVI = "IVI";

	private Set<Integer> iviTypes;
	private Set<Integer> pictogramCategoryCodes;

	public IviDataTypeApi() {
		this.setMessageType(IVI);
	}

	public IviDataTypeApi(String publisherId, String publisherName, String originatingCountry,
						  String protocolVersion, String contentType, Set<String> quadTree,
						  String serviceType, Set<Integer> iviTypes, Set<Integer> pictogramCategoryCodes) {
		super(IVI, publisherId, publisherName, originatingCountry, protocolVersion, contentType, quadTree, serviceType);
		this.iviTypes = iviTypes;
		this.pictogramCategoryCodes = pictogramCategoryCodes;
	}


	public Set<Integer> getIviTypes() {
		return iviTypes;
	}

	public void setIviTypes(Set<Integer> iviTypes) {
		this.iviTypes = iviTypes;
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
		putIntegerValue(values, MessageProperty.IVI_TYPE, this.getIviTypes());
		putIntegerValue(values, MessageProperty.PICTOGRAM_CATEGORY_CODE, this.getPictogramCategoryCodes());
		return values;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof IviDataTypeApi)) return false;
		if (!super.equals(o)) return false;

		IviDataTypeApi that = (IviDataTypeApi) o;

		if (iviTypes != null ? !iviTypes.equals(that.iviTypes) : that.iviTypes != null) return false;
		return pictogramCategoryCodes != null ? pictogramCategoryCodes.equals(that.pictogramCategoryCodes) : that.pictogramCategoryCodes == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (iviTypes != null ? iviTypes.hashCode() : 0);
		result = 31 * result + (pictogramCategoryCodes != null ? pictogramCategoryCodes.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "IviDataTypeApi{" +
				super.baseToString() +
				", iviType=" + iviTypes +
				", pictogramCategoryCodes=" + pictogramCategoryCodes +
				'}';
	}
}
