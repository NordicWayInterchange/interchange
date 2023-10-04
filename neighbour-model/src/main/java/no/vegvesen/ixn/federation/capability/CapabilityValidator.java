package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilityValidator {

    public static boolean validateCapability(CapabilitySplitApi capability) {
        boolean capabilityIsVerified = false;

        ApplicationApi application = capability.getApplication();

        if (application instanceof DatexApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatoryDatex2PropertyNames);
        }
        else if (application instanceof DenmApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatoryDenmPropertyNames);
        }
        else if (application instanceof IvimApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatoryIvimPropertyNames);
        }
        else if (application instanceof SpatemApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (application instanceof MapemApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (application instanceof SremApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (application instanceof SsemApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (application instanceof CamApplicationApi) {
            capabilityIsVerified = checkProperties(application, CapabilityProperty.mandatoryCamPropertyNames);
        }
        return capabilityIsVerified;
    }

    public static boolean checkProperties(ApplicationApi applicationApi, Set<String> mandatoryProperties) {
        for (String property: mandatoryProperties) {
            if (applicationApi.getCommonProperties(applicationApi.getMessageType()).get(property) == null) {
                return false;
            }
        }
        return true;
    }
}
