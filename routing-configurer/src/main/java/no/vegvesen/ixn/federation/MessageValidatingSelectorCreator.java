package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;

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
}
