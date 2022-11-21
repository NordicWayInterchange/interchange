package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Set;

public class IvimCapabilityApi extends CapabilityApi {

	public IvimCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Integer shardCount, String infoUrl) {
		super(Constants.IVIM,publisherId,originatingCountry,protocolVersion,redirect,shardCount,infoUrl,quadTree);
	}

	public IvimCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,null,null,null);
	}

	public IvimCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,redirect,null,null);
	}

	public IvimCapabilityApi() {
		this(null, null, null, null, null);
	}

	@Override
	public String toString() {
		return "IvimCapabilityApi{" +
				"} " + super.toString();
	}
}
