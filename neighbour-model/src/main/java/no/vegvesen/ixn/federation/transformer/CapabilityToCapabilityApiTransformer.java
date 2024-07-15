package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CapabilityToCapabilityApiTransformer {
	private static Logger logger = LoggerFactory.getLogger(CapabilityToCapabilityApiTransformer.class);

	public CapabilityToCapabilityApiTransformer() {
	}

	public Set<CapabilityApi> capabilitiesToCapabilitiesApi(Set<Capability> capabilities) {
		Set<CapabilityApi> capabilityApis = new HashSet<>();
		for (Capability capability : capabilities) {
			CapabilityApi capabilityApi = new CapabilityApi(
					capability.getApplication().toApi(),
					capability.getMetadata().toApi()
			);
			capabilityApis.add(capabilityApi);
		}
		return capabilityApis;
	}

	public Capability capabilityApiToCapability(CapabilityApi capabilityApi) {
		return new Capability(
				applicationApiToApplication(capabilityApi.getApplication()),
				metadataApiToMetadata(capabilityApi.getMetadata())
		);
	}

	public Set<Capability> capabilitiesApiToCapabilities(Set<CapabilityApi> capabilityApis) {
		Set<Capability> capabilities = new HashSet<>();
		for (CapabilityApi capabilityApi : capabilityApis) {
			logger.debug("Converting message type {}", capabilityApi.getApplication().getMessageType());
			capabilities.add(new Capability(
					applicationApiToApplication(capabilityApi.getApplication()),
					metadataApiToMetadata(capabilityApi.getMetadata())
			));
		}
		return capabilities;
	}

	public Set<NeighbourCapability> capabilityApiToNeighbourCapabilities(Set<CapabilityApi> capabilityApis){
		Set<NeighbourCapability> neighbourCapabilities = new HashSet<>();
		for(CapabilityApi capabilityApi : capabilityApis){
			neighbourCapabilities.add(new NeighbourCapability(
					applicationApiToApplication(capabilityApi.getApplication()),
					metadataApiToMetadata(capabilityApi.getMetadata())
			));
		}
		return neighbourCapabilities;
	}

	public CapabilityApi capabilityToCapabilityApi(Capability capability) {
		return new CapabilityApi(
				capability.getApplication().toApi(),
				capability.getMetadata().toApi()
		);
	}

	public Application applicationApiToApplication(ApplicationApi applicationApi) {
		if (applicationApi instanceof DatexApplicationApi) {
			return new DatexApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree(), ((DatexApplicationApi) applicationApi).getPublicationType(), ((DatexApplicationApi) applicationApi).getPublisherName());
		} else if (applicationApi instanceof DenmApplicationApi) {
			return new DenmApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree(), ((DenmApplicationApi) applicationApi).getCauseCode());
		} else if (applicationApi instanceof IvimApplicationApi) {
			return new IvimApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree());
		} else if (applicationApi instanceof SpatemApplicationApi) {
			return new SpatemApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree());
		} else if (applicationApi instanceof MapemApplicationApi) {
			return new MapemApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree());
		} else if (applicationApi instanceof SremApplicationApi) {
			return new SremApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree());
		} else if (applicationApi instanceof SsemApplicationApi) {
			return new SsemApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree());
		} else if (applicationApi instanceof CamApplicationApi) {
			return new CamApplication(applicationApi.getPublisherId(), applicationApi.getPublicationId(), applicationApi.getOriginatingCountry(), applicationApi.getProtocolVersion(), applicationApi.getQuadTree());
		}
		throw new RuntimeException("Subclass of Capability not possible to convert: " + applicationApi.getClass().getSimpleName());
	}

	public Metadata metadataApiToMetadata(MetadataApi metadataApi) {
		Metadata metadata = new Metadata();
		if (metadataApi.getShardCount() != null)
			metadata.setShardCount(metadataApi.getShardCount());
		if (metadataApi.getInfoUrl() != null)
			metadata.setInfoUrl(metadataApi.getInfoUrl());
		if (metadataApi.getMaxBandwidth() != null)
			metadata.setMaxBandwidth(metadataApi.getMaxBandwidth());
		if (metadataApi.getMaxMessageRate() != null)
			metadata.setMaxMessageRate(metadataApi.getMaxMessageRate());
		if (metadataApi.getRepetitionInterval() != null)
			metadata.setRepetitionInterval(metadataApi.getRepetitionInterval());

		metadata.setRedirectPolicy(transformRedirectStatusApiToRedirectStatus(metadataApi.getRedirectPolicy()));
		return metadata;
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
}