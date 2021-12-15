package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.IvimCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(CapabilityApi.IVIM)
public class IvimCapability extends Capability {
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "capability_ivitypes", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capivi_cap")))
	@Column(name = "ivi_type")
	private final Set<String> iviTypes = new HashSet<>();

	public IvimCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> iviTypes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree);
		this.iviTypes.addAll(iviTypes);
	}

	public IvimCapability() {
	}

	public Set<String> getIviTypes() {
		return iviTypes;
	}

	@Override
	public Map<String, String> getSingleValues() {
		return getSingleValuesBase(CapabilityApi.IVIM);
	}

	@Override
	public CapabilityApi toApi() {
		return new IvimCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getIviTypes());
	}

	@Override
	public String messageType() {
		return CapabilityApi.IVIM;
	}

	@Override
	public String toString() {
		return "IvimCapability{" +
				"iviTypes=" + iviTypes +
				"} " + super.toString();
	}
}
