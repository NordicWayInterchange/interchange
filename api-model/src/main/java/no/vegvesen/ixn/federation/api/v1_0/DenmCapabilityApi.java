package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DenmCapabilityApi extends CapabilityApi {
	private Set<String> causeCode = new HashSet<>();

	public DenmCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> causeCode) {
		super(DENM, publisherId, originatingCountry, protocolVersion, quadTree);
		if (causeCode != null) {
			this.causeCode.addAll(causeCode);
		}
	}

	public DenmCapabilityApi() {
		this(null, null, null, null, null);
	}

	public Set<String> getCauseCode() {
		return causeCode;
	}

	public void setCauseCode(Collection<String> causeCode) {
		this.causeCode.clear();
		if (causeCode != null){
			this.causeCode.addAll(causeCode);
		}
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.CAUSE_CODE, this.getCauseCode());
		return values;
	}

}
