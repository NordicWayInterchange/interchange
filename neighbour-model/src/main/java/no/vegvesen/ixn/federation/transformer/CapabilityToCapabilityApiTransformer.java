package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
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

	public Capability capabilityApiToCapability(CapabilityApi capabilityApi) {
		if (capabilityApi instanceof DatexCapabilityApi){
			return new DatexCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((DatexCapabilityApi) capabilityApi).getPublicationType());
		} else if (capabilityApi instanceof DenmCapabilityApi) {
			return new DenmCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((DenmCapabilityApi) capabilityApi).getCauseCode());
		} else if (capabilityApi instanceof IvimCapabilityApi) {
			return new IvimCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((IvimCapabilityApi) capabilityApi).getIviType());
		} else if (capabilityApi instanceof SpatemCapabilityApi) {
			return new SpatemCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((SpatemCapabilityApi) capabilityApi).getIds());
		} else if (capabilityApi instanceof MapemCapabilityApi) {
			return new MapemCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((MapemCapabilityApi) capabilityApi).getIds());
		} else if (capabilityApi instanceof SremCapabilityApi) {
			return new SremCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((SremCapabilityApi) capabilityApi).getIds());
		} else if (capabilityApi instanceof SsemCapabilityApi) {
			return new SsemCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((SsemCapabilityApi) capabilityApi).getIds());
		} else if (capabilityApi instanceof CamCapabilityApi) {
			return new CamCapability(capabilityApi.getPublisherId(), capabilityApi.getOriginatingCountry(), capabilityApi.getProtocolVersion(), capabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getRedirect()), ((CamCapabilityApi) capabilityApi).getStationTypes());
		}
		throw new RuntimeException("Subclass of CapabilityApi not possible to convert: " + capabilityApi.getClass().getSimpleName());
	}

	private RedirectStatus transformRedirectStatusApiToRedirectStatus(RedirectStatusApi status) {
		if (status == null) {
			return RedirectStatus.OPTIONAL;
		}
		switch (status) {
			case MANDATORY:
				return RedirectStatus.MANDATORY;
			case NOT_AVAILABLE:
				return RedirectStatus.NOT_AVAILABLE;
			default:
				return RedirectStatus.OPTIONAL;
		}
	}

	public Capability splitCapabilityApiToCapability(CapabilitySplitApi capabilityApi) {
		if (capabilityApi.getApplication() instanceof DatexCapabilityApplicationApi) {
			return new DatexCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), new HashSet<>(Collections.singleton(((DatexCapabilityApplicationApi) capabilityApi.getApplication()).getPublicationType())));
		} else if (capabilityApi.getApplication() instanceof DenmCapabilityApplicationApi) {
			return new DenmCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), ((DenmCapabilityApplicationApi) capabilityApi.getApplication()).getCauseCode());
		} else if (capabilityApi.getApplication() instanceof IvimCapabilityApplicationApi) {
			return new IvimCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), ((IvimCapabilityApplicationApi) capabilityApi.getApplication()).getIviType());
		} else if (capabilityApi.getApplication() instanceof SpatemCapabilityApplicationApi) {
			return new SpatemCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), ((SpatemCapabilityApplicationApi) capabilityApi.getApplication()).getIds());
		} else if (capabilityApi.getApplication() instanceof MapemCapabilityApplicationApi) {
			return new MapemCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), ((MapemCapabilityApplicationApi) capabilityApi.getApplication()).getIds());
		} else if (capabilityApi.getApplication() instanceof SremCapabilityApplicationApi) {
			return new SremCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), ((SremCapabilityApplicationApi) capabilityApi.getApplication()).getIds());
		} else if (capabilityApi.getApplication() instanceof SsemCapabilityApplicationApi) {
			return new SsemCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), ((SsemCapabilityApplicationApi) capabilityApi.getApplication()).getIds());
		} else if (capabilityApi.getApplication() instanceof CamCapabilityApplicationApi) {
			return new CamCapability(capabilityApi.getApplication().getPublisherId(), capabilityApi.getApplication().getOriginatingCountry(), capabilityApi.getApplication().getProtocolVersion(), capabilityApi.getApplication().getQuadTree(), transformRedirectStatusApiToRedirectStatus(capabilityApi.getMetadata().getRedirectPolicy()), ((CamCapabilityApplicationApi) capabilityApi.getApplication()).getStationTypes());
		}
		throw new RuntimeException("Subclass of Capability not possible to convert: " + capabilityApi.getClass().getSimpleName());
	}

}