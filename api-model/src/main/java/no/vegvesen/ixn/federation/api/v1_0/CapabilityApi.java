package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.vegvesen.ixn.properties.CapabilityProperty;

import java.util.*;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "messageType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = DatexCapabilityApi.class, name = Constants.DATEX_2),
		@JsonSubTypes.Type(value = DenmCapabilityApi.class, name = Constants.DENM),
		@JsonSubTypes.Type(value = IvimCapabilityApi.class, name = Constants.IVIM),
		@JsonSubTypes.Type(value = SpatemCapabilityApi.class, name = Constants.SPATEM),
		@JsonSubTypes.Type(value = MapemCapabilityApi.class, name = Constants.MAPEM),
		@JsonSubTypes.Type(value = SremCapabilityApi.class, name = Constants.SREM),
		@JsonSubTypes.Type(value = SsemCapabilityApi.class, name = Constants.SSEM),
		@JsonSubTypes.Type(value = CamCapabilityApi.class, name = Constants.CAM),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CapabilityApi {

	private String messageType;
	private String publisherId;
	private String originatingCountry;
	private String protocolVersion;
	private final Set<String> quadTree = new HashSet<>();
	private RedirectStatusApi redirect;
	private Integer shardCount = 1;
	private String infoUrl;

	public CapabilityApi(String messageType) {
		if (messageType == null) {
			throw new IllegalArgumentException("messageType can not be null");
		}
		this.messageType = messageType;
	}

	public CapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, RedirectStatusApi redirect, Integer shardCount, String infoUrl,Set<String> quadTree) {
		this(messageType);
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

	public CapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
		this(messageType,publisherId,originatingCountry,protocolVersion,null,null,null, quadTree);
	}

	public CapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect) {
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

	public RedirectStatusApi getRedirect() {
		return redirect;
	}

	public void setRedirect(RedirectStatusApi redirect) {
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

	public Map<String, Object> getCommonProperties(String messageType) {
		Map<String, Object> values = new HashMap<>();
		putValue(values, CapabilityProperty.MESSAGE_TYPE, messageType);
		putValue(values, CapabilityProperty.PUBLISHER_ID, this.getPublisherId());
		putValue(values, CapabilityProperty.ORIGINATING_COUNTRY, this.getOriginatingCountry());
		putValue(values, CapabilityProperty.PROTOCOL_VERSION, this.getProtocolVersion());
		putMultiValue(values, CapabilityProperty.QUAD_TREE, this.getQuadTree());
		return values;
	}

	static void putValue(Map<String, Object> values, CapabilityProperty property, String value) {
		if (value != null && value.length() > 0) {
			values.put(property.getName(), value);
		}
	}

	static void putMultiValue(Map<String, Object> values, CapabilityProperty property, Set<String> multiValue) {
		if (multiValue.isEmpty()) {
			values.put(property.getName(), null);
		} else {
			StringBuilder builder = new StringBuilder();
			for (String value : multiValue) {
				builder.append(value).append(",");
			}
			values.put(property.getName(), builder.toString());
		}
	}

}
