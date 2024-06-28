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

	public Set<CapabilitySplitApi> capabilitiesSplitToCapabilitiesSplitApi(Set<Capability> capabilities) {
		Set<CapabilitySplitApi> capabilitySplitApis = new HashSet<>();
		for (Capability capability : capabilities) {
			CapabilitySplitApi capabilitySplitApi = new CapabilitySplitApi(
					capability.getApplication().toApi(),
					capability.getMetadata().toApi()
			);
			capabilitySplitApis.add(capabilitySplitApi);
		}
		return capabilitySplitApis;
	}

	public Capability capabilitySplitApiToCapabilitySplit(CapabilitySplitApi capabilitySplitApi) {
		return new Capability(
				applicationApiToApplication(capabilitySplitApi.getApplication()),
				metadataApiToMetadata(capabilitySplitApi.getMetadata())
		);
	}

	public Set<Capability> capabilitiesSplitApiToCapabilitiesSplit(Set<CapabilitySplitApi> capabilitySplitApis) {
		Set<Capability> capabilities = new HashSet<>();
		for (CapabilitySplitApi capabilitySplitApi : capabilitySplitApis) {
			logger.debug("Converting message type {}", capabilitySplitApi.getApplication().getMessageType());
			capabilities.add(new Capability(
					applicationApiToApplication(capabilitySplitApi.getApplication()),
					metadataApiToMetadata(capabilitySplitApi.getMetadata())
			));
		}
		return capabilities;
	}

	public Set<NeighbourCapability> capabilitySplitApiToNeighbourCapabilities(Set<CapabilitySplitApi> capabilitySplitApis){
		Set<NeighbourCapability> neighbourCapabilities = new HashSet<>();
		for(CapabilitySplitApi capabilitySplitApi : capabilitySplitApis){
			neighbourCapabilities.add(new NeighbourCapability(
					applicationApiToApplication(capabilitySplitApi.getApplication()),
					metadataApiToMetadata(capabilitySplitApi.getMetadata())
			));
		}
		return neighbourCapabilities;
	}

	public CapabilitySplitApi capabilitySplitToCapabilitySplitApi(Capability capability) {
		return new CapabilitySplitApi(
				capability.getApplication().toApi(),
				capability.getMetadata().toApi()
		);
	}

	public Application applicationApiToApplication(ApplicationApi applicationApi) {
		return switch (applicationApi){
			case DatexApplicationApi datex -> new DatexApplication(datex.getPublisherId(), datex.getPublicationId(), datex.getOriginatingCountry(), datex.getProtocolVersion(), datex.getQuadTree(), datex.getPublicationType(), datex.getPublisherName());
			case DenmApplicationApi denm -> new DenmApplication(denm.getPublisherId(), denm.getPublicationId(), denm.getOriginatingCountry(), denm.getProtocolVersion(), denm.getQuadTree(), denm.getCauseCode());
			case IvimApplicationApi ivim -> new IvimApplication(ivim.getPublisherId(), ivim.getPublicationId(), ivim.getOriginatingCountry(), ivim.getProtocolVersion(), ivim.getQuadTree());
			case SpatemApplicationApi spatem -> new SpatemApplication(spatem.getPublisherId(), spatem.getPublicationId(), spatem.getOriginatingCountry(), spatem.getProtocolVersion(), spatem.getQuadTree());
			case MapemApplicationApi mapem -> new MapemApplication(mapem.getPublisherId(), mapem.getPublicationId(), mapem.getOriginatingCountry(), mapem.getProtocolVersion(), mapem.getQuadTree());
			case SremApplicationApi srem -> new SremApplication(srem.getPublisherId(), srem.getPublicationId(), srem.getOriginatingCountry(), srem.getProtocolVersion(), srem.getQuadTree());
			case SsemApplicationApi ssem -> new SsemApplication(ssem.getPublisherId(), ssem.getPublicationId(), ssem.getOriginatingCountry(), ssem.getProtocolVersion(), ssem.getQuadTree());
			case CamApplicationApi cam -> new CamApplication(cam.getPublisherId(), cam.getPublicationId(), cam.getOriginatingCountry(), cam.getProtocolVersion(), cam.getQuadTree());
			default -> throw new RuntimeException("Subclass of Capability not possible to convert: " + applicationApi.getClass().getSimpleName());
		};
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