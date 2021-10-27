package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.DENM)
public class DenmCapability extends Capability {
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "capability_causecodes", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capcac_cap")))
	@Column(name = "ivi_type")
	private final Set<String> causeCodes = new HashSet<>();

	public DenmCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> causeCodes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree);
		this.causeCodes.addAll(causeCodes);
	}

	public DenmCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect, Set<String> causeCodes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
		this.causeCodes.addAll(causeCodes);
	}

	public DenmCapability() {
	}

	@Override
	public Map<String, String> getSingleValues() {
		return getSingleValuesBase(Constants.DENM);
	}

	@Override
	public CapabilityApi toApi() {
		return new DenmCapabilityApi(this.getPublisherId(), this.getOriginatingCountry(), this.getProtocolVersion(), this.getQuadTree(), this.getCauseCodes());
	}

	@Override
	public String messageType() {
		return Constants.DENM;
	}

	public Set<String> getCauseCodes() {
		return causeCodes;
	}

	@Override
	public String toString() {
		return "DenmCapability{" +
				"causeCodes=" + causeCodes +
				"} " + super.toString();
	}
}
