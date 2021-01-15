package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	@Override
	public Map<String, String> getSingleValues() {
		return getSingleValuesBase(CapabilityApi.DATEX_2);
	}

	@Override
	public CapabilityApi toApi() {
		return new DatexCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getPublicationTypes());
	}

	@Override
	public String toString() {
		return "DatexCapability{" +
				"publicationTypes=" + publicationTypes +
				"} " + super.toString();
	}
}
