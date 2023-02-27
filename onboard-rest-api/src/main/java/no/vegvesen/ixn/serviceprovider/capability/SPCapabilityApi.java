package no.vegvesen.ixn.serviceprovider.capability;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "messageType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = DatexSPCapabilityApi.class, name = Constants.DATEX_2),
		@JsonSubTypes.Type(value = DenmSPCapabilityApi.class, name = Constants.DENM),
		@JsonSubTypes.Type(value = IvimSPCapabilityApi.class, name = Constants.IVIM),
		@JsonSubTypes.Type(value = SpatemSPCapabilityApi.class, name = Constants.SPATEM),
		@JsonSubTypes.Type(value = MapemSPCapabilityApi.class, name = Constants.MAPEM),
		@JsonSubTypes.Type(value = SremSPCapabilityApi.class, name = Constants.SREM),
		@JsonSubTypes.Type(value = SsemSPCapabilityApi.class, name = Constants.SSEM),
		@JsonSubTypes.Type(value = CamSPCapabilityApi.class, name = Constants.CAM),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SPCapabilityApi {

	private String messageType;
	private String publisherId;
	private String originatingCountry;
	private String protocolVersion;
	private final Set<String> quadTree = new HashSet<>();
	private SPRedirectStatusApi redirect;
	private Integer shardCount = 1;
	private String infoUrl;

	public SPCapabilityApi() {
	}

	public SPCapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, SPRedirectStatusApi redirect, Integer shardCount, String infoUrl, Set<String> quadTree) {
		if (messageType == null) {
			throw new IllegalArgumentException("messageType can not be null");
		}
		this.messageType = messageType;
		this.publisherId = publisherId;
		this.originatingCountry = originatingCountry;
		this.protocolVersion = protocolVersion;
		this.redirect = redirect;
		this.shardCount = shardCount;
		this.infoUrl = infoUrl;
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
	}

	public SPCapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
		this(messageType,publisherId,originatingCountry,protocolVersion,null,null,null, quadTree);
	}

	public SPCapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, SPRedirectStatusApi redirect) {
		this(messageType,publisherId,originatingCountry,protocolVersion,redirect,null,null,quadTree);
	}

	public String getOriginatingCountry() {
		return this.originatingCountry;
	}

	public void setOriginatingCountry(String originatingCountry) {
		this.originatingCountry = originatingCountry;
	}

	public String getMessageType() {
		return this.messageType;
	}

	public void setMessageType(String messageType) {
		if (messageType == null) {
			throw new IllegalArgumentException("messageType can not be null");
		}
		this.messageType = messageType;
	}

	public Set<String> getQuadTree() {
		return quadTree;
	}

	public void setQuadTree(Collection<String> quadTree) {
		this.quadTree.clear();
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
	}

	public String getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
	}


	public String getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public SPRedirectStatusApi getRedirect() {
		return redirect;
	}

	public void setRedirect(SPRedirectStatusApi redirect) {
		this.redirect = redirect;
	}

	@Override
	public String toString() {
		return "CapabilityApi{" +
				"messageType='" + messageType + '\'' +
				", publisherId='" + publisherId + '\'' +
				", originatingCountry='" + originatingCountry + '\'' +
				", protocolVersion='" + protocolVersion + '\'' +
				", quadTree=" + quadTree +
				", redirect=" + redirect +
				", shardCount=" + shardCount +
				", infoUrl='" + infoUrl + '\'' +
				'}';
	}

	public Integer getShardCount() {
		return shardCount;
	}

	public void setShardCount(Integer shardCount) {
		this.shardCount = shardCount;
	}

	public String getInfoUrl() {
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl) {
		this.infoUrl = infoUrl;
	}
}
