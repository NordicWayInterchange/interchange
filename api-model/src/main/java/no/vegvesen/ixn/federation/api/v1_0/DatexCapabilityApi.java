package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;

public class DatexCapabilityApi extends CapabilityApi {
	private Set<String> publicationType = new HashSet<>();

	public DatexCapabilityApi(){
	}

	public DatexCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> publicationType) {
		super(DATEX_2, publisherId, originatingCountry, protocolVersion, quadTree);
		if (publicationType != null) {
			this.publicationType.addAll(publicationType);
		}
	}

	public DatexCapabilityApi(String originatingCountry){
		this(null, originatingCountry, null, Collections.emptySet(), Collections.emptySet());
	}

	public Set<String> getPublicationType() {
		return publicationType;
	}

	public void setPublicationType(Collection<String> publicationType) {
		this.publicationType.clear();
		if (publicationType != null) {
			this.publicationType.addAll(publicationType);
		}
	}

	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = super.getValues();
		putValue(values, MessageProperty.PUBLICATION_TYPE, this.getPublicationType());
		return values;
	}



}
