package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.shared.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
					capability.getMetadata().toApi(capability.getShardCount())
			);
			capabilityApis.add(capabilityApi);
		}
		return capabilityApis;
	}

	public Capability capabilityApiToCapability(CapabilityApi capabilityApi) {
		return new Capability(
				applicationApiToApplication(capabilityApi.getApplication()),
				metadataApiToMetadata(capabilityApi.getMetadata()),
				buildShards(capabilityApi)
		);
	}

	public Set<Capability> capabilitiesApiToCapabilities(Set<CapabilityApi> capabilityApis) {
		Set<Capability> capabilities = new HashSet<>();
		for (CapabilityApi capabilityApi : capabilityApis) {
			logger.debug("Converting message type {}", capabilityApi.getApplication().getMessageType());
			capabilities.add(new Capability(
					applicationApiToApplication(capabilityApi.getApplication()),
					metadataApiToMetadata(capabilityApi.getMetadata()),
					buildShards(capabilityApi)
			));
		}
		return capabilities;
	}

	public Set<NeighbourCapability> capabilityApiToNeighbourCapabilities(Set<CapabilityApi> capabilityApis){
		Set<NeighbourCapability> neighbourCapabilities = new HashSet<>();
		for(CapabilityApi capabilityApi : capabilityApis){
			int shardCount = capabilityApi.getMetadata().getShardCount() != null
					? capabilityApi.getMetadata().getShardCount()
					: 1;
			neighbourCapabilities.add(new NeighbourCapability(
					applicationApiToApplication(capabilityApi.getApplication()),
					metadataApiToMetadata(capabilityApi.getMetadata()),
					shardCount
			));
		}
		return neighbourCapabilities;
	}

	public CapabilityApi capabilityToCapabilityApi(Capability capability) {
		return new CapabilityApi(
				capability.getApplication().toApi(),
				capability.getMetadata().toApi(capability.getShardCount())
		);
	}

	public CapabilityApi neighbourCapabilityToCapabilityApi(NeighbourCapability capability) {
		return new CapabilityApi(
				capability.getApplication().toApi(),
				capability.getMetadata().toApi(capability.getShardCount())
		);
	}

	private List<CapabilityShard> buildShards(CapabilityApi capabilityApi) {
		int shardCount = capabilityApi.getMetadata().getShardCount() != null
				? capabilityApi.getMetadata().getShardCount()
				: 1;
		boolean isSharded = shardCount > 1;
		List<CapabilityShard> shards = new ArrayList<>();
		for (int i = 0; i < shardCount; i++) {
			String exchangeName = "cap-" + UUID.randomUUID();
			Integer shardId = i + 1;
			String selector = MessageValidatingSelectorCreator.makeSelector(capabilityApi, isSharded ? shardId : null);
			shards.add(new CapabilityShard(shardId, exchangeName, selector));
		}
		return shards;
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
