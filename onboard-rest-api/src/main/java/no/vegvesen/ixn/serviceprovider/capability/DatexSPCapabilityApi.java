package no.vegvesen.ixn.serviceprovider.capability;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DatexSPCapabilityApi extends SPCapabilityApi {
	private Set<String> publicationType = new HashSet<>();

	public DatexSPCapabilityApi(){
	}

	public DatexSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> publicationType) {
		super(Constants.DATEX_2,publisherId,originatingCountry,protocolVersion,redirect,shardCount,infoUrl,quadTree);
		if (publicationType != null) {
			this.publicationType.addAll(publicationType);
		}
	}
	public DatexSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> publicationType) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,null,null,null,publicationType);
	}

	public DatexSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Set<String> publicationType) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,redirect,null,null,publicationType);
	}

	public DatexSPCapabilityApi(String originatingCountry){
		this(null, originatingCountry, null, Collections.emptySet(), SPRedirectStatusApi.OPTIONAL, Collections.emptySet());
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
