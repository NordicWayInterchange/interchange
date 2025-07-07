package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.capability.Application;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.selector.SelectorBuilder;

public class MessageValidatingSelectorCreator {


    public static String makeSelector(Application application, Integer shardId) {
        String messageType = application.getMessageType();
        SelectorBuilder builder = new SelectorBuilder()
                .publisherId(application.getPublisherId())
                .publicationId(application.getPublicationId())
                .originatingCountry(application.getOriginatingCountry())
                .protocolVersion(application.getProtocolVersion())
                .quadTree(application.getQuadTree())
                .messageType(messageType);
        if (messageType.equals(Constants.DENM)) {
            DenmApplication denmApplication = (DenmApplication) application;
            builder.causeCode(denmApplication.getCauseCode());
        } else if (messageType.equals(Constants.DATEX_2)) {
            DatexApplication datexApplication = (DatexApplication) application;
            builder.publicationTypes(datexApplication.getPublicationType());
        }
        if (shardId != null) {
            builder.shardId(shardId.toString());
        }
        return builder.toSelector();
    }
}
