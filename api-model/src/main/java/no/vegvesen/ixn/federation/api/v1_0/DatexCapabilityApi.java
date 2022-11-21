package no.vegvesen.ixn.federation.api.v1_0;

import java.util.*;

public class DatexCapabilityApi extends CapabilityApi {
	private Set<String> publicationType = new HashSet<>();

	public DatexCapabilityApi(){
	}

	public DatexCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree,RedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> publicationType) {
		super(Constants.DATEX_2,publisherId,originatingCountry,protocolVersion,redirect,shardCount,infoUrl,quadTree);
		if (publicationType != null) {
			this.publicationType.addAll(publicationType);
		}
	}
	public DatexCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> publicationType) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,null,null,null,publicationType);
	}

	public DatexCapabilityApi(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect, Set<String> publicationType) {
		this(publisherId,originatingCountry,protocolVersion,quadTree,redirect,null,null,publicationType);
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
	public HashMap<String, String> getProperties() {
		HashMap<String, String> properties = new HashMap<>();
		properties.put("messageType", super.getMessageType());
		properties.put("publisherId", super.getPublisherId());
		properties.put("originatingCountry", super.getOriginatingCountry());
		properties.put("protocolVersion", super.getProtocolVersion());
		properties.put("quadTree", super.getQuadTree().toString());
		properties.put("publicationType", publicationType.toString());
		return properties;
	}

	@Override
	public String toString() {
		return "DatexCapabilityApi{" +
				"publicationType=" + publicationType +
				"} " + super.toString();
	}
}
