package no.vegvesen.ixn.serviceprovider.capability;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DenmSPCapabilityApi extends SPCapabilityApi {
	private Set<String> causeCode = new HashSet<>();

	public DenmSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, SPRedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> quadTree, Set<String> causeCode) {
		super(Constants.DENM,publisherId,originatingCountry,protocolVersion,redirect,shardCount,infoUrl,quadTree);
		if (causeCode != null) {
			this.causeCode.addAll(causeCode);
		}
	}

	public DenmSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> causeCode) {
		this(publisherId,originatingCountry,protocolVersion,null,null,null,quadTree,causeCode);
	}

	public DenmSPCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect, Set<String> causeCode) {
		this(publisherId,originatingCountry,protocolVersion,redirect,null,null,quadTree,causeCode);
	}

	public DenmSPCapabilityApi() {
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
	public String toString() {
		return "DenmCapabilityApi{" +
				"causeCode=" + causeCode +
				"} " + super.toString();
	}
}
