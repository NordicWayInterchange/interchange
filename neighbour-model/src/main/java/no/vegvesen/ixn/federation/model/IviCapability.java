package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.IviCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.RedirectStatusApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.IVI)
public class IviCapability extends Capability {
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "capability_ivitypes", joinColumns = @JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name="fk_capivi_cap")))
	@Column(name = "ivi_type")
	private final Set<String> iviTypes = new HashSet<>();

	public IviCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<String> iviTypes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree);
		this.iviTypes.addAll(iviTypes);
	}

	public IviCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect, Set<String> iviTypes) {
		super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
		this.iviTypes.addAll(iviTypes);
	}

	public IviCapability() {
	}

	public Set<String> getIviTypes() {
		return iviTypes;
	}

	@Override
	public Map<String, String> getSingleValues() {
		return getSingleValuesBase(Constants.IVI);
	}

	@Override
	public CapabilityApi toApi() {
		return new IviCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()), getIviTypes());
	}

	@Override
	public RedirectStatusApi toRedirectStatusApi(RedirectStatus status) {
		switch (status) {
			case MANDATORY:
				return RedirectStatusApi.MANDATORY;
			case NOT_AVAILABLE:
				return RedirectStatusApi.NOT_AVAILABLE;
			default:
				return RedirectStatusApi.OPTIONAL;
		}
	}

	@Override
	public String messageType() {
		return Constants.IVI;
	}

	@Override
	public String toString() {
		return "IviCapability{" +
				"iviTypes=" + iviTypes +
				"} " + super.toString();
	}
}
