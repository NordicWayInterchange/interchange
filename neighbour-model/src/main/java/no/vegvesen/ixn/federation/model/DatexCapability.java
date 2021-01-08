package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.capability.CapabilityFilter;
import no.vegvesen.ixn.properties.MessageProperty;

import javax.persistence.*;
import java.util.*;

@Entity
@DiscriminatorValue(CapabilityApi.DATEX_2)
public class DatexCapability extends Capability{
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "capability_publications", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_cappub_cap")))
	@Column(name = "publication_type")
	private final Set<String> publicationTypes = new HashSet<>();

	public DatexCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> publicationTypes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree);
		if (publicationTypes != null) {
			this.publicationTypes.addAll(publicationTypes);
		}
	}

	public DatexCapability() {
	}

	public Set<String> getPublicationTypes() {
		return publicationTypes;
	}

	public void setPublicationTypes(Set<String> publicationTypes) {
		this.publicationTypes.clear();
		if (publicationTypes != null){
			this.publicationTypes.addAll(publicationTypes);
		}
	}

	@Transient
	@Override
	public List<CapabilityFilter> getCapabilityFiltersFlat() {
		List<CapabilityFilter> capabilitiesFilters = new LinkedList<>();
		for (String quadTreeTile : noEmptySet(getQuadTree())) {
			for (String publicationType : noEmptySet(this.publicationTypes)) {
				Map<String, String> singleCapability = getValues();
				singleCapability.put(MessageProperty.MESSAGE_TYPE.getName(), CapabilityApi.DATEX_2);
				singleCapability.put(MessageProperty.QUAD_TREE.getName(), quadTreeTile);
				singleCapability.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
				capabilitiesFilters.add(new CapabilityFilter(singleCapability));
			}
		}
		return capabilitiesFilters;
	}

	@Override
	public CapabilityApi toApi() {
		return new DatexCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getPublicationTypes());
	}
}
