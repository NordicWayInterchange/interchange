package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;
import no.vegvesen.ixn.federation.capability.CapabilityFilter;
import no.vegvesen.ixn.properties.MessageProperty;

import javax.persistence.*;
import java.util.*;

@Entity
@DiscriminatorValue(CapabilityApi.DENM)
public class DenmCapability extends Capability {
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "capability_causecodes", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capcac_cap")))
	@Column(name = "ivi_type")
	private final Set<String> causeCodes = new HashSet<>();

	public DenmCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> causeCodes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree);
		this.causeCodes.addAll(causeCodes);
	}

	public DenmCapability() {
	}

	@Transient
	@Override
	public List<CapabilityFilter> getCapabilityFiltersFlat() {
		List<CapabilityFilter> capabilitiesFilters = new LinkedList<>();
		for (String quadTreeTile : noEmptySet(getQuadTree())) {
			for (String causeCode : noEmptySet(causeCodes)) {
				Map<String, String> singleCapability = getValues();
				singleCapability.put(MessageProperty.MESSAGE_TYPE.getName(), CapabilityApi.DENM);
				singleCapability.put(MessageProperty.QUAD_TREE.getName(), quadTreeTile);
				singleCapability.put(MessageProperty.CAUSE_CODE.getName(), causeCode);
				capabilitiesFilters.add(new CapabilityFilter(singleCapability));
			}
		}
		return capabilitiesFilters;
	}

	@Override
	public CapabilityApi toApi() {
		return new DenmCapabilityApi(this.getPublisherId(), this.getOriginatingCountry(), this.getProtocolVersion(), this.getQuadTree(), this.getCauseCodes());
	}

	public Set<String> getCauseCodes() {
		return causeCodes;
	}
}
