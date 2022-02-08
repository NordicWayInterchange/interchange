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

	public Set<CapabilityApi> capabilitiesToCapabilityApis(Set<Capability> capabilities) {
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

	//TODO: Re-evaluate, put in null-checks on RedirectStatus but that should not be there now?
	public Capability capabilityApiToCapability(CapabilityApi capabilityApi) {
		if (capabilityApi instanceof DatexCapabilityApi){
			if(capabilityApi.getRedirect() != null){
				return new DatexCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((DatexCapabilityApi) capabilityApi).getPublicationType());
			} else {
				return new DatexCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((DatexCapabilityApi) capabilityApi).getPublicationType());
			}
		}
		else if (capabilityApi instanceof DenmCapabilityApi) {
			if(capabilityApi.getRedirect() != null) {
				return new DenmCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((DenmCapabilityApi) capabilityApi).getCauseCode());
			} else {
				return new DenmCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((DenmCapabilityApi) capabilityApi).getCauseCode());
			}
		}
		else if (capabilityApi instanceof IvimCapabilityApi) {
			if(capabilityApi.getRedirect() != null) {
				return new IvimCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((IvimCapabilityApi) capabilityApi).getIviType());
			} else {
				return new IvimCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), ((IvimCapabilityApi) capabilityApi).getIviType());
			}
		}
		throw new RuntimeException("Subclass of CapabilityApi not possible to convert: " + capabilityApi.getClass().getSimpleName());
	}

	private RedirectStatus transformRedirectStatusApiToRedirectStatus(RedirectStatusApi status) {
		switch (status) {
			case MANDATORY:
				return RedirectStatus.MANDATORY;
			case NOT_AVAILABLE:
				return RedirectStatus.NOT_AVAILABLE;
			default:
				return RedirectStatus.OPTIONAL;
		}
	}

}