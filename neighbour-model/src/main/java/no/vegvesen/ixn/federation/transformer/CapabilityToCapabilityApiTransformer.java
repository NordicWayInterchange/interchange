package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.IvimCapabilityApi;
import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.model.IvimCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class CapabilityToCapabilityApiTransformer {
	private static Logger logger = LoggerFactory.getLogger(CapabilityToCapabilityApiTransformer.class);

	public CapabilityToCapabilityApiTransformer() {
	}

	Set<CapabilityApi> capabilitiesToCapabilityApis(Set<Capability> capabilities) {
		Set<CapabilityApi> apis = new HashSet<>();
		for (Capability capability : capabilities) {
			CapabilityApi capabilityApi = capability.toApi();
			if (capabilityApi != null) {
				apis.add(capabilityApi);
			}
		}
		return apis;
	}

	public Set<Capability> capabilitiesApiToCapabilities(Set<? extends CapabilityApi> capabilityApis) {
		Set<Capability> capabilities = new HashSet<>();
		for (CapabilityApi capability : capabilityApis) {
			logger.debug("Converting message type {}", capability.getMessageType());
			capabilities.add(capabilityApiToCapability(capability));
		}
		return capabilities;
	}

	public Capability capabilityApiToCapability(CapabilityApi capabilityApi) {
		if (capabilityApi instanceof DatexCapabilityApi){
			return new DatexCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((DatexCapabilityApi) capabilityApi).getPublicationType());
		}
		else if (capabilityApi instanceof DenmCapabilityApi) {
			return new DenmCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((DenmCapabilityApi) capabilityApi).getCauseCode());
		}
		else if (capabilityApi instanceof IvimCapabilityApi) {
			return new IvimCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((IvimCapabilityApi) capabilityApi).getIviType());
		}
		throw new RuntimeException("Subclass of CapabilityApi not possible to convert: " + capabilityApi.getClass().getSimpleName());
	}


}