package no.vegvesen.ixn.federation.capability;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilityVerifier {

    public static boolean isValid(CapabilityApi capability) {
        String messageType = capability.getMessageType();
        Set<String> mandatoryPropertyNames;

        switch(messageType) {
            case Constants.DATEX_2:
                mandatoryPropertyNames = CapabilityProperty.mandatoryDatex2PropertyNames;
                break;
            case Constants.DENM:
                mandatoryPropertyNames = CapabilityProperty.mandatoryDenmPropertyNames;
                break;
            case Constants.IVIM:
                mandatoryPropertyNames = CapabilityProperty.mandatoryIvimPropertyNames;
                break;
            case Constants.SPATEM:
                mandatoryPropertyNames = CapabilityProperty.mandatorySpatemMapemPropertyNames;
                break;
            case Constants.MAPEM:
                mandatoryPropertyNames = CapabilityProperty.mandatorySpatemMapemPropertyNames;
                break;
            case Constants.SREM:
                mandatoryPropertyNames = CapabilityProperty.mandatorySremSsemPropertyNames;
                break;
            case Constants.SSEM:
                mandatoryPropertyNames = CapabilityProperty.mandatorySremSsemPropertyNames;
                break;
            case Constants.CAM:
                mandatoryPropertyNames = CapabilityProperty.mandatoryCamPropertyNames;
                break;
            default:
                return false;
        }
        return validateProperties(capability, mandatoryPropertyNames);
    }

    private static boolean validateProperties(CapabilityApi capability, Set<String> properties) {
        for (String property : properties) {
            if (capability.getProperties().get(property) == null) {
                return false;
            }
        }
        return true;
    }
}
