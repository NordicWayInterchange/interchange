package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.model.IviCapability;

public class MessageValidatingSelectorCreator {


    public static String makeSelector(Capability capability) {
        String messageType = capability.messageType();
        SelectorBuilder builder = new SelectorBuilder()
                .publisherId(capability.getPublisherId())
                .originatingCountry(capability.getOriginatingCountry())
                .protocolVersion(capability.getProtocolVersion())
                .quadTree(capability.getQuadTree())
                .messageType(messageType);
        if (messageType.equals(Constants.IVI)) {
            IviCapability iviCapability = (IviCapability) capability;
            builder.iviTypes(iviCapability.getIviTypes());
        } else if (messageType.equals(Constants.DENM)) {
            DenmCapability denmCapability = (DenmCapability) capability;
            builder.causeCodes(denmCapability.getCauseCodes());
        } else if (messageType.equals(Constants.DATEX_2)) {
            DatexCapability datexCapability = (DatexCapability) capability;
            builder.publicationTypes(datexCapability.getPublicationTypes());
        }
        return builder.toSelector();
    }
}
