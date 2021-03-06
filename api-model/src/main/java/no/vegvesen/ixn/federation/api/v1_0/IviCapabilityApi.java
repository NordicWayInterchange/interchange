package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IviCapabilityApi extends CapabilityApi {
	private Set<String> iviType = new HashSet<>();

	public IviCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> iviType) {
		super(IVI, publisherId, originatingCountry, protocolVersion, quadTree);
		if (iviType != null) {
			this.iviType.addAll(iviType);
		}
	}

	public IviCapabilityApi() {
		this(null, null, null, null, null);
	}

	public Set<String> getIviType() {
		return iviType;
	}

	public void setIviType(Collection<String> iviType) {
		this.iviType.clear();
		if (this.iviType != null){
			this.iviType.addAll(iviType);
		}
	}


	@Override
	public String toString() {
		return "IviCapabilityApi{" +
				"iviType=" + iviType +
				"} " + super.toString();
	}
}
