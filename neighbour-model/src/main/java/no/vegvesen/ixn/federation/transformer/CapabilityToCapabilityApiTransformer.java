package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
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

	public ApplicationApi applicationToApplicationApi(Application application){
		return switch (application){
			case DatexApplication datex -> new DatexApplicationApi(datex.getPublisherId(), datex.getPublicationId(), datex.getOriginatingCountry(), datex.getProtocolVersion(), datex.getQuadTree(), datex.getPublicationType(), datex.getPublisherName());
			case DenmApplication denm -> new DenmApplicationApi(denm.getPublisherId(), denm.getPublicationId(), denm.getOriginatingCountry(), denm.getProtocolVersion(), denm.getQuadTree(), denm.getCauseCode());
			case IvimApplication ivim -> new IvimApplicationApi(ivim.getPublisherId(), ivim.getPublicationId(), ivim.getOriginatingCountry(), ivim.getProtocolVersion(), ivim.getQuadTree());
			case SpatemApplication spatem -> new SpatemApplicationApi(spatem.getPublisherId(), spatem.getPublicationId(), spatem.getOriginatingCountry(), spatem.getProtocolVersion(), spatem.getQuadTree());
			case MapemApplication mapem -> new MapemApplicationApi(mapem.getPublisherId(), mapem.getPublicationId(), mapem.getOriginatingCountry(), mapem.getProtocolVersion(), mapem.getQuadTree());
			case SremApplication srem -> new SremApplicationApi(srem.getPublisherId(), srem.getPublicationId(), srem.getOriginatingCountry(), srem.getProtocolVersion(), srem.getQuadTree());
			case SsemApplication ssem -> new SsemApplicationApi(ssem.getPublisherId(), ssem.getPublicationId(), ssem.getOriginatingCountry(), ssem.getProtocolVersion(), ssem.getQuadTree());
			case CamApplication cam -> new CamApplicationApi(cam.getPublisherId(), cam.getPublicationId(), cam.getOriginatingCountry(), cam.getProtocolVersion(), cam.getQuadTree());
			default -> throw new RuntimeException("Subclass of Capability not possible to convert: " + application.getClass().getSimpleName());
		};
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

	public MetadataApi metadataToMetadataApi(Metadata metadata){
		MetadataApi metadataApi = new MetadataApi();
		if(metadata.getShardCount() != null){
			metadataApi.setShardCount(metadata.getShardCount());
		}
		if(metadata.getInfoUrl() != null){
			metadataApi.setInfoUrl(metadata.getInfoUrl());
		}
		if(metadata.getMaxBandwidth() != null){
			metadataApi.setMaxBandwidth(metadata.getMaxBandwidth());
		}
		if(metadata.getMaxMessageRate() != null){
			metadataApi.setMaxMessageRate(metadata.getMaxMessageRate());
		}
		if(metadata.getRepetitionInterval() != null){
			metadataApi.setRepetitionInterval(metadata.getRepetitionInterval());
		}
		metadataApi.setRedirectPolicy(transformRedirectStatusToRedirectStatusApi(metadata.getRedirectPolicy()));
		return metadataApi;
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

	private RedirectStatusApi transformRedirectStatusToRedirectStatusApi(RedirectStatus status){
		if(status == null){
			return RedirectStatusApi.OPTIONAL;
		}
		return switch (status){
			case RedirectStatus.MANDATORY -> RedirectStatusApi.MANDATORY;
			case RedirectStatus.NOT_AVAILABLE -> RedirectStatusApi.NOT_AVAILABLE;
			default -> RedirectStatusApi.OPTIONAL;
		};
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