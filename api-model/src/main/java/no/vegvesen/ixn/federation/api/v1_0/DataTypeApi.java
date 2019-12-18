package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.vegvesen.ixn.properties.MessageProperty;

import java.util.*;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "messageType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = Datex2DataTypeApi.class, name = Datex2DataTypeApi.DATEX_2),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTypeApi implements DataTypeI {

	private String messageType;
	private String publisherId;
	private String publisherName;
	private String originatingCountry;
	private Set<String> quadTree = new HashSet<>();

	public DataTypeApi() {
	}

	public DataTypeApi(String messageType, String publisherId, String publisherName, String originatingCountry, Set<String> quadTree) {
		this.messageType = messageType;
		this.publisherId = publisherId;
		this.publisherName = publisherName;
		this.originatingCountry = originatingCountry;
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
	}

	@Override
	public String getOriginatingCountry() {
		return this.originatingCountry;
	}

	@Override
	public void setOriginatingCountry(String originatingCountry) {
		this.originatingCountry = originatingCountry;
	}

	@Override
	public String getMessageType() {
		return this.messageType;
	}

	@Override
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	@Override
	public Set<String> getQuadTree() {
		return quadTree;
	}

	@Override
	public void setQuadTree(Collection<String> quadTree) {
		this.quadTree.clear();
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
	}

	@Override
	public String getPublisherId() {
		return publisherId;
	}

	@Override
	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
	}

	@Override
	public String getPublisherName() {
		return publisherName;
	}

	@Override
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DataTypeApi)) return false;

		DataTypeApi that = (DataTypeApi) o;

		if (!messageType.equals(that.messageType)) return false;
		if (publisherId != null ? !publisherId.equals(that.publisherId) : that.publisherId != null) return false;
		if (publisherName != null ? !publisherName.equals(that.publisherName) : that.publisherName != null)
			return false;
		if (originatingCountry != null ? !originatingCountry.equals(that.originatingCountry) : that.originatingCountry != null)
			return false;
		return quadTree != null ? quadTree.equals(that.quadTree) : that.quadTree == null;
	}

	@Override
	public int hashCode() {
		int result = messageType.hashCode();
		result = 31 * result + (publisherId != null ? publisherId.hashCode() : 0);
		result = 31 * result + (publisherName != null ? publisherName.hashCode() : 0);
		result = 31 * result + (originatingCountry != null ? originatingCountry.hashCode() : 0);
		result = 31 * result + (quadTree != null ? quadTree.hashCode() : 0);
		return result;
	}

	public Map<String, String> getValues() {
		Map<String, String> values = new HashMap<>();
		putValue(values, MessageProperty.MESSAGE_TYPE, this.getMessageType());
		putValue(values, MessageProperty.PUBLISHER_ID, this.getPublisherId());
		putValue(values, MessageProperty.PUBLISHER_NAME, this.getPublisherName());
		putValue(values, MessageProperty.ORIGINATING_COUNTRY, this.getOriginatingCountry());
		putValue(values, MessageProperty.QUAD_TREE, this.getQuadTree());
		return values;
	}

	void putValue(Map<String, String> values, MessageProperty messageProperty, Set<String> value) {
		if (value != null && value.size() > 0) {
			String join = String.join(",", value);
			values.put(messageProperty.getName(), join);
		}
	}

	void putValue(Map<String, String> values, MessageProperty messageProperty, String value) {
		if (value != null && value.length() > 0) {
			values.put(messageProperty.getName(), value);
		}
	}
}
