package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.IvimCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.RedirectStatusApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.IVIM)
public class IvimCapability extends Capability {

	public IvimCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
		super(publisherId, originatingCountry, protocolVersion, quadTree);
	}

	public IvimCapability(String publisherId, String originatingCountry, String protocolVersion, Set<String> quadTree, RedirectStatus redirect) {
		super(publisherId, originatingCountry, protocolVersion, quadTree, redirect);
	}

	public IvimCapability() {
	}

	@Override
	public Map<String, String> getSingleValues() {
		return getSingleValuesBase(Constants.IVIM);
	}

	@Override
	public CapabilityApi toApi() {
		return new IvimCapabilityApi(getPublisherId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), toRedirectStatusApi(getRedirect()));
	}

	@Override
	public String messageType() {
		return Constants.IVIM;
	}

	@Override
	public String toString() {
		return "IvimCapability{" +
				"} " + super.toString();
	}
}
