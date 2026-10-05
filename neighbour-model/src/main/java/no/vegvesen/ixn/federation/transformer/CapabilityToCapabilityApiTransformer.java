package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.shared.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CapabilityToCapabilityApiTransformer {
	private static final Logger logger = LoggerFactory.getLogger(CapabilityToCapabilityApiTransformer.class);

	public CapabilityToCapabilityApiTransformer() {
	}

	public Set<CapabilityApi> capabilitiesToCapabilitiesApi(Set<Capability> capabilities) {
		Set<CapabilityApi> capabilityApis = new HashSet<>();
		for (Capability capability : capabilities) {
			Metadata metadata = capability.getMetadata();
            CapabilityApi capabilityApi = new CapabilityApi(
					applicationApiToApplicationApi(capability.getApplication()),
					metadataToMetadataApi(metadata)
			);
			capabilityApis.add(capabilityApi);
		}
		return capabilityApis;
	}

	private static MetadataApi metadataToMetadataApi(Metadata metadata) {
		return new MetadataApi(metadata.getShardCount(), metadata.getInfoUrl(), redirectStatusToRedirectStatusApi(metadata.getRedirectPolicy()), metadata.getMaxBandwidth(), metadata.getMaxMessageRate(), metadata.getRepetitionInterval());
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
				applicationApiToApplicationApi(capability.getApplication()),
				metadataToMetadataApi(capability.getMetadata())
		);
	}

	public CapabilityApi neighbourCapabilityToCapabilityApi(NeighbourCapability capability) {
		return new CapabilityApi(
				applicationApiToApplicationApi(capability.getApplication()),
				metadataToMetadataApi(capability.getMetadata())
		);
	}

	private static RedirectStatusApi redirectStatusToRedirectStatusApi(RedirectStatus status) {
        if (status != null) {
			return switch (status) {
				case MANDATORY -> RedirectStatusApi.MANDATORY;
				case NOT_AVAILABLE -> RedirectStatusApi.NOT_AVAILABLE;
				default -> RedirectStatusApi.OPTIONAL;
			};
		}
		return RedirectStatusApi.OPTIONAL;
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
		else
			metadata.setShardCount(1);
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

	private static ApplicationApi applicationApiToApplicationApi(Application application) {
		return switch (application) {
			case DatexApplication d -> new DatexApplicationApi(
					d.getPublisherId(),
					d.getPublicationId(),
					d.getOriginatingCountry(),
					d.getProtocolVersion(),
					d.getQuadTree(),
					d.getPublicationType(),
					d.getPublisherName()
			);
			case DenmApplication d ->  new DenmApplicationApi(
					d.getPublisherId(),
					d.getPublicationId(),
					d.getOriginatingCountry(),
					d.getProtocolVersion(),
					d.getQuadTree(),
					d.getCauseCode()
			);
			case IvimApplication i ->  new IvimApplicationApi(
					i.getPublisherId(),
					i.getPublicationId(),
					i.getOriginatingCountry(),
					i.getProtocolVersion(),
					i.getQuadTree()
			);
			case SpatemApplication sp ->  new SpatemApplicationApi(
					sp.getPublisherId(),
					sp.getPublicationId(),
					sp.getOriginatingCountry(),
					sp.getProtocolVersion(),
					sp.getQuadTree()
			);
			case MapemApplication mapem ->  new MapemApplicationApi(
					mapem.getPublisherId(),
					mapem.getPublicationId(),
					mapem.getOriginatingCountry(),
					mapem.getProtocolVersion(),
					mapem.getQuadTree()
			);
			case SremApplication srem ->  new SremApplicationApi(
					srem.getPublisherId(),
					srem.getPublicationId(),
					srem.getOriginatingCountry(),
					srem.getProtocolVersion(),
					srem.getQuadTree()
			);
			case SsemApplication ssem ->  new SsemApplicationApi(
					ssem.getPublisherId(),
					ssem.getPublicationId(),
					ssem.getOriginatingCountry(),
					ssem.getProtocolVersion(),
					ssem.getQuadTree()
			);
			case CamApplication cam ->  new CamApplicationApi(
					cam.getPublisherId(),
					cam.getPublicationId(),
					cam.getOriginatingCountry(),
					cam.getProtocolVersion(),
					cam.getQuadTree()
			);
			default -> throw new IllegalArgumentException("Unknown application api");
		};

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