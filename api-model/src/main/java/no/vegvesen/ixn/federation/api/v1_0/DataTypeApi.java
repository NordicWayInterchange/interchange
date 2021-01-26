package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "messageType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = Datex2DataTypeApi.class, name = Datex2DataTypeApi.DATEX_2),
		@JsonSubTypes.Type(value = DenmDataTypeApi.class, name = DenmDataTypeApi.DENM),
		@JsonSubTypes.Type(value = IviDataTypeApi.class, name = IviDataTypeApi.IVI),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTypeApi {

	private String messageType;
	private String publisherId;
	private String originatingCountry;
	private String protocolVersion;
	private Set<String> quadTree = new HashSet<>();

	public DataTypeApi() {
	}

	public DataTypeApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DataTypeApi that = (DataTypeApi) o;
		return Objects.equals(messageType, that.messageType) &&
				Objects.equals(publisherId, that.publisherId) &&
				Objects.equals(originatingCountry, that.originatingCountry) &&
				Objects.equals(protocolVersion, that.protocolVersion) &&
				Objects.equals(quadTree, that.quadTree);
	}

	@Override
	public int hashCode() {
		return Objects.hash(messageType, publisherId, originatingCountry, protocolVersion, quadTree);
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

	@SuppressWarnings("SameParameterValue")
	static void putIntegerValue(Map<String, String> values, MessageProperty messageProperty, Set<Integer> value) {
		if (value != null && value.size() > 0) {
			Set<String> stringIntegers = Stream.of(value).flatMap(Collection::stream).map(Object::toString).collect(Collectors.toSet());
			String join = String.join(",", stringIntegers);
			values.put(messageProperty.getName(), join);
		}
	}

	static void putValue(Map<String, String> values, MessageProperty messageProperty, String value) {
		if (value != null && value.length() > 0) {
			values.put(messageProperty.getName(), value);
		}
	}

	@SuppressWarnings("SameParameterValue")
	static void putValue(Map<String, String> values, MessageProperty messageProperty, Integer value) {
		if (value != null) {
			values.put(messageProperty.getName(), value.toString());
		}
	}

	public String baseToString() {
		return 	"messageType='" + messageType + '\'' +
				", publisherId='" + publisherId + '\'' +
				", originatingCountry='" + originatingCountry + '\'' +
				", protocolVersion='" + protocolVersion + '\'' +
				", quadTree=" + quadTree;
	}


}
