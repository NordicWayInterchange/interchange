package no.vegvesen.ixn.federation.selector;

import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.shared.Constants;
import no.vegvesen.ixn.shared.capability.CapabilityApi;
import no.vegvesen.ixn.shared.capability.DatexApplicationApi;
import no.vegvesen.ixn.shared.capability.DenmApplicationApi;

public class MessageValidatingSelectorCreator {


    public static String makeSelector(Capability capability, Integer shardId) {
        String messageType = capability.getApplication().getMessageType();
        SelectorBuilder builder = new SelectorBuilder()
                .publisherId(capability.getApplication().getPublisherId())
                .publicationId(capability.getApplication().getPublicationId())
                .originatingCountry(capability.getApplication().getOriginatingCountry())
                .protocolVersion(capability.getApplication().getProtocolVersion())
                .quadTree(capability.getApplication().getQuadTree())
                .messageType(messageType);
        if (messageType.equals(Constants.DENM)) {
            DenmApplication denmApplication = (DenmApplication) capability.getApplication();
            builder.causeCode(denmApplication.getCauseCode());
        } else if (messageType.equals(Constants.DATEX_2)) {
            DatexApplication datexApplication = (DatexApplication) capability.getApplication();
            builder.publicationTypes(datexApplication.getPublicationType());
        }
        if (shardId != null) {
            builder.shardId(shardId.toString());
        }
        return builder.toSelector();
    }

    public static String makeSelector(CapabilityApi capabilityApi, Integer shardId) {
        String messageType = capabilityApi.getApplication().getMessageType();
        SelectorBuilder builder = new SelectorBuilder()
                .publisherId(capabilityApi.getApplication().getPublisherId())
                .publicationId(capabilityApi.getApplication().getPublicationId())
                .originatingCountry(capabilityApi.getApplication().getOriginatingCountry())
                .protocolVersion(capabilityApi.getApplication().getProtocolVersion())
                .quadTree(capabilityApi.getApplication().getQuadTree())
                .messageType(messageType);
        if (messageType.equals(Constants.DENM)) {
            DenmApplicationApi denmApplicationApi = (DenmApplicationApi) capabilityApi.getApplication();
            builder.causeCode(denmApplicationApi.getCauseCode());
        } else if (messageType.equals(Constants.DATEX_2)) {
            DatexApplicationApi datexApplicationApi = (DatexApplicationApi) capabilityApi.getApplication();
            builder.publicationTypes(datexApplicationApi.getPublicationType());
        }
        if (shardId != null) {
            builder.shardId(shardId.toString());
        }
        return builder.toSelector();
    }
}
