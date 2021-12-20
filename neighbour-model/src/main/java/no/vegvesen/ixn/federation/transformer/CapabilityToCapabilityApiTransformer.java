package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.*;
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
		else if (capabilityApi instanceof SpatemCapabilityApi) {
			return new SpatemCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((SpatemCapabilityApi) capabilityApi).getIds());
		}
		else if (capabilityApi instanceof MapemCapabilityApi) {
			return new MapemCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((MapemCapabilityApi) capabilityApi).getIds());
		}
		else if (capabilityApi instanceof SremCapabilityApi) {
			return new SremCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((SremCapabilityApi) capabilityApi).getIds());
		}
		else if (capabilityApi instanceof SsemCapabilityApi) {
			return new SsemCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((SsemCapabilityApi) capabilityApi).getIds());
		}
		else if (capabilityApi instanceof CamCapabilityApi) {
			return new CamCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((CamCapabilityApi) capabilityApi).getStationTypes());
		}
		throw new RuntimeException("Subclass of CapabilityApi not possible to convert: " + capabilityApi.getClass().getSimpleName());
	}


}