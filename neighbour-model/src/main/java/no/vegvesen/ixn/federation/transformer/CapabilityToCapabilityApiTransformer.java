package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.capability.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

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
		Application application = applicationApiToApplication(capabilityApi.getApplication());
		return new Capability(
				application,
				metadataApiToMetadata(capabilityApi.getMetadata()),
				createCapabilityShards(application,capabilityApi.getMetadata().getShardCount())
		);
	}

	private List<CapabilityShard> createCapabilityShards(Application application, Integer shardCount) {
		List<CapabilityShard> capabilityShards = new ArrayList<>();
		for (int i = 0; i < shardCount; i++) {
			String exchangeName = "cap-" + UUID.randomUUID();
			String capabilitySelector;
			if (shardCount > 1) {
				capabilitySelector = MessageValidatingSelectorCreator.makeSelector(application, i+1);
			} else {
				capabilitySelector = MessageValidatingSelectorCreator.makeSelector(application,null);
			}
			CapabilityShard shard = new CapabilityShard(i + 1,exchangeName, capabilitySelector);
			capabilityShards.add(shard);
		}
		return capabilityShards;
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

	public CapabilityApi neighbourCapabilityToCapabilityApi(NeighbourCapability capability) {
		return new CapabilityApi(
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
		int shardCount;
		if (metadataApi.getShardCount() != null) {
			shardCount = metadataApi.getShardCount();
		} else {
			shardCount = 1;
		}
        return new Metadata(
                metadataApi.getInfoUrl(),
                shardCount,
                transformRedirectStatusApiToRedirectStatus(metadataApi.getRedirectPolicy()),
                metadataApi.getMaxBandwidth(),
                metadataApi.getMaxMessageRate(),
                metadataApi.getRepetitionInterval()
        );
	}

	private RedirectStatus transformRedirectStatusApiToRedirectStatus(RedirectStatusApi status) {
		if (status == null) {
			return RedirectStatus.OPTIONAL;
		}
        return switch (status) {
            case MANDATORY -> RedirectStatus.MANDATORY;
            case NOT_AVAILABLE -> RedirectStatus.NOT_AVAILABLE;
            default -> RedirectStatus.OPTIONAL;
        };
	}
}