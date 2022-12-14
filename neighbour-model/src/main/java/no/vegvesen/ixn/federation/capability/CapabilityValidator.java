package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import no.vegvesen.ixn.serviceprovider.model.AddCapabilitiesRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CapabilityValidator {

    private static final Logger logger = LoggerFactory.getLogger(CapabilityValidator.class);

    public static AddCapabilitiesRequest validateServiceProviderCapabilities(AddCapabilitiesRequest capabilities) {
        Set<CapabilityApi> validatedCapailities = new HashSet<>();
        for (CapabilityApi capability : capabilities.getCapabilities()) {
            if (validateCapability(capability)) {
                validatedCapailities.add(capability);
            }
        }
        return new AddCapabilitiesRequest(capabilities.getName(), validatedCapailities);
    }

    public static boolean validateCapability(CapabilityApi capability) {
        boolean capabilityIsVerified = false;

        if (capability instanceof DatexCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatoryDatex2PropertyNames);
        }
        else if (capability instanceof DenmCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatoryDenmPropertyNames);
        }
        else if (capability instanceof IvimCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatoryIvimPropertyNames);
        }
        else if (capability instanceof SpatemCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (capability instanceof MapemCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (capability instanceof SremCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (capability instanceof SsemCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (capability instanceof CamCapabilityApi) {
            capabilityIsVerified = checkProperties(capability, CapabilityProperty.mandatoryCamPropertyNames);
        }
        return capabilityIsVerified;
    }

    public static boolean checkProperties(CapabilityApi capability, Set<String> mandatoryProperties) {
        for (String property: mandatoryProperties) {
            if (capability.getCommonProperties(capability.getMessageType()).get(property) == null) {
                logger.info("Capability property {} is not set properly for capability {}", property, capability);
                return false;
            }
        }
        return true;
    }
}
