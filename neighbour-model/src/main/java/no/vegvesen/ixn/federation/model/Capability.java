package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
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

	public Capability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
		this.publisherId = publisherId;
		this.originatingCountry = originatingCountry;
		this.protocolVersion = protocolVersion;
		if (quadTree != null) {
			this.quadTree.addAll(quadTree);
		}
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

	public abstract CapabilityApi toApi();


	@Override
	public String toString() {
		return "Capability{" +
				"id=" + id +
				", publisherId='" + publisherId + '\'' +
				", originatingCountry='" + originatingCountry + '\'' +
				", protocolVersion='" + protocolVersion + '\'' +
				", quadTree=" + quadTree +
				'}';
	}
}
