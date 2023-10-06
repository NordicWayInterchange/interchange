package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilityValidator {

    private static final Logger logger = LoggerFactory.getLogger(CapabilityValidator.class);

    public static boolean capabilityIsValid(CapabilitySplitApi capability) {
        boolean capabilityIsValid = false;

        ApplicationApi application = capability.getApplication();

        if (application instanceof DatexApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatoryDatex2PropertyNames);
        }
        else if (application instanceof DenmApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatoryDenmPropertyNames);
        }
        else if (application instanceof IvimApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatoryIvimPropertyNames);
        }
        else if (application instanceof SpatemApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (application instanceof MapemApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (application instanceof SremApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (application instanceof SsemApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (application instanceof CamApplicationApi) {
            capabilityIsValid = checkProperties(application, CapabilityProperty.mandatoryCamPropertyNames);
        }
        return capabilityIsValid;
    }

    public static boolean checkProperties(ApplicationApi applicationApi, Set<String> mandatoryProperties) {
        for (String property: mandatoryProperties) {
            if (applicationApi.getCommonProperties(applicationApi.getMessageType()).get(property) == null) {
                logger.info("Capability property {} is not set for application {}", property, applicationApi);
                return false;
            }
        }
        return true;
    }
}
