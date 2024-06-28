package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CapabilityValidator {

    private static final Logger logger = LoggerFactory.getLogger(CapabilityValidator.class);

    public static Set<String> capabilityIsValid(CapabilitySplitApi capability) {
        ApplicationApi application = capability.getApplication();

        return switch (application){
            case DatexApplicationApi datex -> checkProperties(datex, CapabilityProperty.mandatoryDatex2PropertyNames);
            case DenmApplicationApi denm -> checkProperties(denm, CapabilityProperty.mandatoryDatex2PropertyNames);
            case IvimApplicationApi ivim -> checkProperties(ivim, CapabilityProperty.mandatoryDatex2PropertyNames);
            case SpatemApplicationApi spatem -> checkProperties(spatem, CapabilityProperty.mandatoryDatex2PropertyNames);
            case MapemApplicationApi mapem -> checkProperties(mapem, CapabilityProperty.mandatoryDatex2PropertyNames);
            case SremApplicationApi srem -> checkProperties(srem, CapabilityProperty.mandatoryDatex2PropertyNames);
            case SsemApplicationApi ssem -> checkProperties(ssem, CapabilityProperty.mandatoryDatex2PropertyNames);
            case CamApplicationApi cam -> checkProperties(cam, CapabilityProperty.mandatoryDatex2PropertyNames);
            default -> throw new IllegalStateException("Error occurred while validating capability");
        };
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

    public static boolean quadtreeIsValid(CapabilitySplitApi capability){
        List<String> quadTree = capability.getApplication().getQuadTree();
        for(String quadTile : quadTree){
            for(char nextNumber : quadTile.toCharArray()){
                if(Character.getNumericValue(nextNumber) > 3 || Character.getNumericValue(nextNumber) < 0){
                    return false;
                }
            }
        }
        return true;
    }
}
