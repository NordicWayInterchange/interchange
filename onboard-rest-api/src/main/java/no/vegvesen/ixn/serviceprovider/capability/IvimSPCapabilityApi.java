package no.vegvesen.ixn.serviceprovider.capability;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IvimSPCapabilityApi extends SPCapabilityApi {
	private Set<String> iviType = new HashSet<>();

	public IvimSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> iviType) {
		super(Constants.IVIM,publisherId,originatingCountry,protocolVersion,redirect,shardCount,infoUrl,quadTree);
		if (iviType != null) {
			this.iviType.addAll(iviType);
		}
	}

	public IvimSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> iviType) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,null,null,null,iviType);
	}

	public IvimSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Set<String> iviType) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,redirect,null,null,iviType);
	}

	public IvimSPCapabilityApi() {
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
		return "IvimCapabilityApi{" +
				"iviType=" + iviType +
				"} " + super.toString();
	}
}
