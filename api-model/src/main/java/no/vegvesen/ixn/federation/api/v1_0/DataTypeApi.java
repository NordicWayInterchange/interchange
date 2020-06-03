package no.vegvesen.ixn.federation.api.v1_0;

/*-
 * #%L
 * api-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
	private String publisherName;
	private String originatingCountry;
	private String protocolVersion;
	private String contentType;
	private Set<String> quadTree = new HashSet<>();

	public DataTypeApi() {
	}

	public DataTypeApi(String messageType, String publisherId, String publisherName, String originatingCountry, String protocolVersion, String contentType, Set<String> quadTree) {
		if (messageType == null) {
			throw new IllegalArgumentException("messageType can not be null");
		}
		this.messageType = messageType;
		this.publisherId = publisherId;
		this.publisherName = publisherName;
		this.originatingCountry = originatingCountry;
		this.protocolVersion = protocolVersion;
		this.contentType = contentType;
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

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}


	public String getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
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
		if (protocolVersion != null ? !protocolVersion.equals(that.protocolVersion) : that.protocolVersion != null)
			return false;
		if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null) return false;
		return quadTree != null ? quadTree.equals(that.quadTree) : that.quadTree == null;
	}

	@Override
	public int hashCode() {
		int result = messageType.hashCode();
		result = 31 * result + (publisherId != null ? publisherId.hashCode() : 0);
		result = 31 * result + (publisherName != null ? publisherName.hashCode() : 0);
		result = 31 * result + (originatingCountry != null ? originatingCountry.hashCode() : 0);
		result = 31 * result + (protocolVersion != null ? protocolVersion.hashCode() : 0);
		result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
		result = 31 * result + (quadTree != null ? quadTree.hashCode() : 0);
		return result;
	}

	@JsonIgnore
	public Map<String, String> getValues() {
		Map<String, String> values = new HashMap<>();
		putValue(values, MessageProperty.MESSAGE_TYPE, this.getMessageType());
		putValue(values, MessageProperty.PUBLISHER_ID, this.getPublisherId());
		putValue(values, MessageProperty.PUBLISHER_NAME, this.getPublisherName());
		putValue(values, MessageProperty.ORIGINATING_COUNTRY, this.getOriginatingCountry());
		putValue(values, MessageProperty.PROTOCOL_VERSION, this.getProtocolVersion());
		putValue(values, MessageProperty.CONTENT_TYPE, this.getContentType());
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
}
