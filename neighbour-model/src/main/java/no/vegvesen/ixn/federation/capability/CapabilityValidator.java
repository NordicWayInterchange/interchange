package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.properties.CapabilityProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CapabilityValidator {

    private static final Logger logger = LoggerFactory.getLogger(CapabilityValidator.class);

    private static Pattern validCharacters = Pattern.compile("[A-Z0-9a-z.:-]*");

    private static Pattern countryCodeRegex = Pattern.compile("[A-Z]{2}");

    private static Pattern publisherIdRegex = Pattern.compile("[A-Z]{2}[0-9]{5}");

    public static Set<String> capabilityIsValid(CapabilityApi capability) {
        ApplicationApi application = capability.getApplication();

        return switch (application){
            case DatexApplicationApi datex -> checkProperties(datex, CapabilityProperty.mandatoryDatex2PropertyNames);
            case DenmApplicationApi denm -> checkProperties(denm, CapabilityProperty.mandatoryDenmPropertyNames);
            case IvimApplicationApi ivim -> checkProperties(ivim, CapabilityProperty.mandatoryIvimPropertyNames);
            case SpatemApplicationApi spatem -> checkProperties(spatem, CapabilityProperty.mandatorySpatemMapemPropertyNames);
            case MapemApplicationApi mapem -> checkProperties(mapem, CapabilityProperty.mandatorySpatemMapemPropertyNames);
            case SremApplicationApi srem -> checkProperties(srem, CapabilityProperty.mandatorySremSsemPropertyNames);
            case SsemApplicationApi ssem -> checkProperties(ssem, CapabilityProperty.mandatorySremSsemPropertyNames);
            case CamApplicationApi cam -> checkProperties(cam, CapabilityProperty.mandatoryCamPropertyNames);
            default -> throw new IllegalStateException("Error occurred while validating capability");
        };
    }

    public static Map<Boolean, String> capabilityHasValidProperties(CapabilityApi capability){
        ApplicationApi application = capability.getApplication();

        return switch (application){
            case DatexApplicationApi datex -> validateProperties(datex, CapabilityProperty.mandatoryDatex2PropertyNames);
            case DenmApplicationApi denm -> validateProperties(denm, CapabilityProperty.mandatoryDenmPropertyNames);
            case IvimApplicationApi ivim -> validateProperties(ivim, CapabilityProperty.mandatoryIvimPropertyNames);
            case SpatemApplicationApi spatem -> validateProperties(spatem, CapabilityProperty.mandatorySpatemMapemPropertyNames);
            case MapemApplicationApi mapem -> validateProperties(mapem, CapabilityProperty.mandatorySpatemMapemPropertyNames);
            case SremApplicationApi srem -> validateProperties(srem, CapabilityProperty.mandatorySremSsemPropertyNames);
            case SsemApplicationApi ssem -> validateProperties(ssem, CapabilityProperty.mandatorySremSsemPropertyNames);
            case CamApplicationApi cam -> validateProperties(cam, CapabilityProperty.mandatoryCamPropertyNames);
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

    public static Map<Boolean, String> validateProperties(ApplicationApi applicationApi, Set<String> mandatoryProperties) {
        for (String property : mandatoryProperties) {

            String value = (String) applicationApi.getCommonProperties(applicationApi.getMessageType()).get(property);
            Matcher validCharMatcher = validCharacters.matcher(value);

            if (!validCharMatcher.matches() && !property.equals("quadTree") && !property.equals("causeCode")) {
                return Map.of(false, String.format("%s contains illegal characters", property));
            }
            if (value.length() > 255 && !property.equals("quadTree") && !property.equals("causeCode")) {
                return Map.of(false, String.format("%s exceeds character limit of 255", property));
            }
            switch (property) {
                case "publisherId" -> {
                    Matcher publisherIdMatcher = publisherIdRegex.matcher(value);
                    if(!publisherIdMatcher.matches()) {
                        return Map.of(false, String.format("%s must be in format <country code><5 numbers>", property));
                    }
                }
                case "originatingCountry" -> {
                    Matcher countryCodeMatcher = countryCodeRegex.matcher(value);
                    if (!countryCodeMatcher.matches()) {
                        return Map.of(false, String.format("'%s' is not a valid country code", value));
                    }
                }
                case "publicationId" -> {
                    String publisherId = (String) applicationApi.getCommonProperties(applicationApi.getMessageType()).get("publisherId");
                    if (!value.startsWith(publisherId + ":")) {
                        return Map.of(false, String.format("%s must start with '<publisherId>:'", property));
                    }
                }
                case "quadTree" -> {
                    String[] quadTreeTiles = value.split(",");
                    for (String quadTreeTile : quadTreeTiles) {
                        if (quadTreeTile.length() > 255) {
                            return Map.of(false, String.format("quadTreeTile '%s' exceeds character limit of 255", quadTreeTile));
                        }
                    }
                }
            }
        }
        return Map.of();
    }

    public static boolean isQuadTreeValid(List<String> quadTreeTiles){
        for(String quadTile : quadTreeTiles){
            for(char nextNumber : quadTile.toCharArray()){
                if(Character.getNumericValue(nextNumber) > 3 || Character.getNumericValue(nextNumber) < 0){
                    return false;
                }
            }
        }
        return true;
    }
}
