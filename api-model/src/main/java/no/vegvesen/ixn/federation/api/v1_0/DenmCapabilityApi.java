package no.vegvesen.ixn.federation.api.v1_0;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DenmCapabilityApi extends CapabilityApi {
	private Set<String> causeCode = new HashSet<>();

	public DenmCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, RedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> quadTree, Set<String> causeCode) {
		super(Constants.DENM,publisherId,originatingCountry,protocolVersion,redirect,shardCount,infoUrl,quadTree);
		if (causeCode != null) {
			this.causeCode.addAll(causeCode);
		}
	}

	public DenmCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> causeCode) {
		this(publisherId,originatingCountry,protocolVersion,null,null,null,quadTree,causeCode);
	}

	public DenmCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Set<String> causeCode) {
		this(publisherId,originatingCountry,protocolVersion,redirect,null,null,quadTree,causeCode);
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
	public HashMap<String, String> getProperties() {
		HashMap<String, String> properties = new HashMap<>();
		properties.put("messageType", super.getMessageType());
		properties.put("publisherId", super.getPublisherId());
		properties.put("originatingCountry", super.getOriginatingCountry());
		properties.put("protocolVersion", super.getProtocolVersion());
		properties.put("quadTree", super.getQuadTree().toString());
		properties.put("causeCode", causeCode.toString());
		return properties;
	}

	@Override
	public String toString() {
		return "DenmCapabilityApi{" +
				"causeCode=" + causeCode +
				"} " + super.toString();
	}
}
