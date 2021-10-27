package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static no.vegvesen.ixn.federation.api.v1_0.CapabilityApi.*;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		visible = true,
		property = "messageType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = DatexCapabilityApi.class, name = Constants.DATEX_2),
		@JsonSubTypes.Type(value = DenmCapabilityApi.class, name = Constants.DENM),
		@JsonSubTypes.Type(value = IviCapabilityApi.class, name = Constants.IVI),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CapabilityApi {

	private String messageType;
	private String publisherId;
	private String originatingCountry;
	private String protocolVersion;
	private final Set<String> quadTree = new HashSet<>();
	private RedirectStatusApi redirect;

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

	public CapabilityApi(String messageType, String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatusApi redirect) {
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
		this.redirect = redirect;
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
				'}';
	}
}
