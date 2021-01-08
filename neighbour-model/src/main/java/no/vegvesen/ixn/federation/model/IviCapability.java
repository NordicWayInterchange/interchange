package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.IviCapabilityApi;
import no.vegvesen.ixn.federation.capability.CapabilityFilter;
import no.vegvesen.ixn.properties.MessageProperty;

import javax.persistence.*;
import java.util.*;

@Entity
@DiscriminatorValue(CapabilityApi.IVI)
public class IviCapability extends Capability {
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "capability_ivitypes", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capivi_cap")))
	@Column(name = "ivi_type")
	private final Set<String> iviTypes = new HashSet<>();

	public IviCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> iviTypes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree);
		this.iviTypes.addAll(iviTypes);
	}

	public IviCapability() {
	}

	public Set<String> getIviTypes() {
		return iviTypes;
	}

	@Transient
	@Override
	public List<CapabilityFilter> getCapabilityFiltersFlat() {
		List<CapabilityFilter> capabilitiesFilters = new LinkedList<>();
		for (String quadTreeTile : noEmptySet(getQuadTree())) {
			for (String iviType : noEmptySet(iviTypes)) {
				Map<String, String> singleCapability = getValues();
				singleCapability.put(MessageProperty.MESSAGE_TYPE.getName(), CapabilityApi.IVI);
				singleCapability.put(MessageProperty.QUAD_TREE.getName(), quadTreeTile);
				singleCapability.put(MessageProperty.IVI_TYPE.getName(), iviType);
				capabilitiesFilters.add(new CapabilityFilter(singleCapability));
			}
		}
		return capabilitiesFilters;
	}

	@Override
	public CapabilityApi toApi() {
		return new IviCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getIviTypes());
	}

}
