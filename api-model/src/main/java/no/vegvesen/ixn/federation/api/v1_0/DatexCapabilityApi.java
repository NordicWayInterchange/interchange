package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DatexCapabilityApi extends CapabilityApi {
	private Set<String> publicationType = new HashSet<>();

	public DatexCapabilityApi(){
	}

	public DatexCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> publicationType) {
		super(Constants.DATEX_2, publisherId, originatingCountry, protocolVersion, quadTree);
		if (publicationType != null) {
			this.publicationType.addAll(publicationType);
		}
	}

	public DatexCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Set<String> publicationType) {
		super(Constants.DATEX_2, publisherId, originatingCountry, protocolVersion, quadTree, redirect);
		if (publicationType != null) {
			this.publicationType.addAll(publicationType);
		}
	}

	public DatexCapabilityApi(String originatingCountry){
		this(null, originatingCountry, null, Collections.emptySet(), RedirectStatusApi.OPTIONAL, Collections.emptySet());
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
	public String toString() {
		return "DatexCapabilityApi{" +
				"publicationType=" + publicationType +
				"} " + super.toString();
	}
}
