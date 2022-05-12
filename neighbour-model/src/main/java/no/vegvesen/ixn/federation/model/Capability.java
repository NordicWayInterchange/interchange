package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.RedirectStatusApi;
import no.vegvesen.ixn.properties.MessageProperty;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "capability")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "messageType")
public abstract class Capability {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_seq")
	private Integer id;

	private String publisherId;
	private String originatingCountry;
	private String protocolVersion;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "capability_quad", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capquad_cap")))
	@Column(name = "quadrant")
	private final Set<String> quadTree = new HashSet<>();

	private CapabilityStatus status = CapabilityStatus.CREATED;

	private RedirectStatus redirect;

	public Capability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
		this.publisherId = publisherId;
		this.originatingCountry = originatingCountry;
		this.protocolVersion = protocolVersion;
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
	}

	public Capability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect) {
		this.publisherId = publisherId;
		this.originatingCountry = originatingCountry;
		this.protocolVersion = protocolVersion;
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
		this.redirect = redirect;
	}

	public Capability() {
	}

	public String getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
	}

	public String getOriginatingCountry() {
		return originatingCountry;
	}

	public void setOriginatingCountry(String originatingCountry) {
		this.originatingCountry = originatingCountry;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public Set<String> getQuadTree() {
		return quadTree;
	}

	public void setQuadTree(Set<String> quadTree) {
		this.quadTree.clear();
		if (quadTree != null){
			this.quadTree.addAll(quadTree);
		}
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public CapabilityStatus getStatus() {
		return status;
	}

	public void setStatus(CapabilityStatus status) {
		this.status = status;
	}

	public RedirectStatus getRedirect() {
		return redirect;
	}

	public void setRedirect(RedirectStatus redirect) {
		this.redirect = redirect;
	}

	public abstract Map<String, String> getSingleValues();

	protected Map<String, String> getSingleValuesBase(String messageType) {
		Map<String, String> values = new HashMap<>();
		putValue(values, MessageProperty.PUBLISHER_ID, this.getPublisherId());
		putValue(values, MessageProperty.ORIGINATING_COUNTRY, this.getOriginatingCountry());
		putValue(values, MessageProperty.PROTOCOL_VERSION, this.getProtocolVersion());
		putValue(values, MessageProperty.MESSAGE_TYPE, messageType);
		return values;
	}

	static void putValue(Map<String, String> values, MessageProperty messageProperty, String value) {
		if (value != null && value.length() > 0) {
			values.put(messageProperty.getName(), value);
		}
	}

	public RedirectStatusApi toRedirectStatusApi(RedirectStatus status) {
		if (status == null) {
			return RedirectStatusApi.OPTIONAL;
		}
		switch (status) {
			case MANDATORY:
				return RedirectStatusApi.MANDATORY;
			case NOT_AVAILABLE:
				return RedirectStatusApi.NOT_AVAILABLE;
			default:
				return RedirectStatusApi.OPTIONAL;
		}
	}

	public abstract CapabilityApi toApi();

	public abstract String messageType();

	@Override
	public String toString() {
		return "Capability{" +
				"id=" + id +
				", publisherId='" + publisherId + '\'' +
				", originatingCountry='" + originatingCountry + '\'' +
				", protocolVersion='" + protocolVersion + '\'' +
				", quadTree=" + quadTree +
				", redirect=" + redirect +
				'}';
	}
}
