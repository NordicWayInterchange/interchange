package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;

import static no.vegvesen.ixn.federation.api.v1_0.CapabilityApi.*;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "messageType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = DatexCapabilityApi.class, name = DATEX_2),
		@JsonSubTypes.Type(value = DenmCapabilityApi.class, name = DENM),
		@JsonSubTypes.Type(value = IviCapabilityApi.class, name = IVI),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CapabilityApi {
	public static final String DATEX_2 = "DATEX2";
	public static final String DENM = "DENM";
	public static final String IVI = "IVI";

	private String messageType;
	private String publisherId;
	private String originatingCountry;
	private String protocolVersion;
	private final Set<String> quadTree = new HashSet<>();

	public CapabilityApi() {
	}

	public CapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
		if (messageType == null) {
			throw new IllegalArgumentException("messageType can not be null");
		}
		this.messageType = messageType;
		this.publisherId = publisherId;
		this.originatingCountry = originatingCountry;
		this.protocolVersion = protocolVersion;
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
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

	@JsonIgnore
	public Map<String, String> getValues() {
		Map<String, String> values = new HashMap<>();
		putValue(values, MessageProperty.MESSAGE_TYPE, this.getMessageType());
		putValue(values, MessageProperty.PUBLISHER_ID, this.getPublisherId());
		putValue(values, MessageProperty.ORIGINATING_COUNTRY, this.getOriginatingCountry());
		putValue(values, MessageProperty.PROTOCOL_VERSION, this.getProtocolVersion());
		putValue(values, MessageProperty.QUAD_TREE, this.getQuadTree());
		return values;
	}

	static void putValue(Map<String, String> values, MessageProperty messageProperty, Set<String> value) {
		if (value != null && value.size() > 0) {
			String join = String.join(",", value);
			values.put(messageProperty.getName(), join);
		}
	}

	static void putValue(Map<String, String> values, MessageProperty messageProperty, String value) {
		if (value != null && value.length() > 0) {
			values.put(messageProperty.getName(), value);
		}
	}

	@Override
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append(this.getClass().getSimpleName());
		string.append("{");
		Map<String, String> values = getValues();
		for (String key : values.keySet()) {
			string.append("\"");
			string.append(key);
			string.append("\":");
			string.append("\"");
			string.append(values.get(key));
			string.append("\"");
		}
		string.append("}");
		return string.toString();
	}
}
