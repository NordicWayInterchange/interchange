package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DenmCapability;
import no.vegvesen.ixn.federation.model.IviCapability;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class MessageValidatingSelectorCreator {


    //TODO make this use MessageProperties
    //TODO should I use not null or not empty?
    public String makeSelector(Capability capability) {
        Set<String> matchings = new HashSet<>();
        if (! Objects.isNull(capability.getPublisherId())) {
            matchings.add(String.format("publisherId = '%s'",capability.getPublisherId()));
        }
        if (! Objects.isNull(capability.getOriginatingCountry())) {
            matchings.add(String.format("originatingCountry = '%s'",capability.getOriginatingCountry()));
        }
        if (! Objects.isNull(capability.getProtocolVersion())) {
            matchings.add(String.format("protocolVersion = '%s'",capability.getProtocolVersion()));
        }
        if (! Objects.isNull(capability.getQuadTree())) {
            matchings.add("quadTree is not null");
        }
        if ( capability instanceof  DenmCapability) {
            matchings.add("messageType = 'DENM'");
            DenmCapability denmCapability = (DenmCapability) capability;
            if (!Objects.isNull(denmCapability.getCauseCodes())) {
                matchings.add("causeCode is not null");
            }
        } else if (capability instanceof IviCapability) {
            matchings.add("messageType = 'IVI'");
            IviCapability iviCapability = (IviCapability) capability;
            if (! Objects.isNull(iviCapability.getIviTypes())) {
                matchings.add("iviType is not null");
            }
        }

        return String.join(" and ",matchings);
    }
}
