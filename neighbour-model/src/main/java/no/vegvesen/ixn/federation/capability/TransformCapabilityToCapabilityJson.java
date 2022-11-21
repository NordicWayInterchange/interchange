package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.Capability;

public class TransformCapabilityToCapabilityJson {

    public static CommonCapabilityJsonObject transformAllCommonProperties(Capability capability) {
        CommonCapabilityJsonObject jsonObject = new CommonCapabilityJsonObject(
                capability.messageType(),
                capability.getPublisherId(),
                capability.getOriginatingCountry(),
                capability.getProtocolVersion());
        return jsonObject;
    }
}
