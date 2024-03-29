package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CapabilityValidator {

    private static final Logger logger = LoggerFactory.getLogger(CapabilityValidator.class);

    public static Set<String> capabilityIsValid(CapabilitySplitApi capability) {
        ApplicationApi application = capability.getApplication();

        if (application instanceof DatexApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatoryDatex2PropertyNames);
        }
        else if (application instanceof DenmApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatoryDenmPropertyNames);
        }
        else if (application instanceof IvimApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatoryIvimPropertyNames);
        }
        else if (application instanceof SpatemApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (application instanceof MapemApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatorySpatemMapemPropertyNames);
        }
        else if (application instanceof SremApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (application instanceof SsemApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatorySremSsemPropertyNames);
        }
        else if (application instanceof CamApplicationApi) {
            return checkProperties(application, CapabilityProperty.mandatoryCamPropertyNames);
        } else {
            throw new IllegalStateException("Error occurred while validating capability");
        }
    }

    public static Set<String> checkProperties(ApplicationApi applicationApi, Set<String> mandatoryProperties) {
        Set<String> notSetProperties = new HashSet<>();
        for (String property: mandatoryProperties) {
            if (applicationApi.getCommonProperties(applicationApi.getMessageType()).get(property) == null) {
                notSetProperties.add(property);
                logger.info("Capability property {} is not set for application {}", property, applicationApi);
            }
        }
        return notSetProperties;
    }
}
