package no.vegvesen.ixn.properties;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapabilityProperty {

    public static final CapabilityProperty MESSAGE_TYPE = new CapabilityProperty("messageType", true, true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty PUBLISHER_ID = new CapabilityProperty("publisherId", true, true, CapabilityPropertyType.STRING);
    //public static final CapabilityProperty PUBLICATION_ID = new CapabilityProperty("publicationId", true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty ORIGINATING_COUNTRY = new CapabilityProperty("originatingCountry", true, true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty PROTOCOL_VERSION = new CapabilityProperty("protocolVersion", true, true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty QUAD_TREE = new CapabilityProperty("quadTree", true, true, CapabilityPropertyType.STRING_ARRAY);

    public static final CapabilityProperty INFO_URL = new CapabilityProperty("infoUrl", false, true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty SERVICE_TYPE = new CapabilityProperty("serviceType", false, true, CapabilityPropertyType.STRING);

    public static final List<CapabilityProperty> commonCapabilityProperties = Arrays.asList(
            MESSAGE_TYPE,
            PUBLISHER_ID,
            ORIGINATING_COUNTRY,
            PROTOCOL_VERSION,
            QUAD_TREE,
            INFO_URL,
            SERVICE_TYPE
    );

    public static final CapabilityProperty PUBLICATION_TYPE = new CapabilityProperty("publicationType", true, true, CapabilityPropertyType.STRING_ARRAY);
    public static final CapabilityProperty PUBLICATION_SUB_TYPE = new CapabilityProperty("publicationSubType", false, true, CapabilityPropertyType.STRING);

    public static final List<CapabilityProperty> datex2CapabilityProperties = Arrays.asList(
            PUBLICATION_TYPE,
            PUBLICATION_SUB_TYPE
    );

    public static final CapabilityProperty CAUSE_CODE = new CapabilityProperty("causeCode", true, true, CapabilityPropertyType.STRING_ARRAY);
    public static final CapabilityProperty SUB_CAUSE_CODE = new CapabilityProperty("subCauseCode", false, true, CapabilityPropertyType.STRING);

    public static final List<CapabilityProperty> denmCapabilityProperties = Arrays.asList(
            CAUSE_CODE
    );

    public static final CapabilityProperty IDS = new CapabilityProperty("id", false, true, CapabilityPropertyType.STRING_ARRAY);
    public static final CapabilityProperty NAME = new CapabilityProperty("name", false, true, CapabilityPropertyType.STRING);

    public static final List<CapabilityProperty> spatemMapemCapabilityProperties = Arrays.asList(
            IDS,
            NAME
    );

    public static Set<String> mandatoryDatex2PropertyNames = Stream.of(
            commonCapabilityProperties,
            datex2CapabilityProperties).flatMap(Collection::stream)
            .filter(CapabilityProperty::isMandatory)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryDenmPropertyNames = Stream.of(
            commonCapabilityProperties,
            denmCapabilityProperties).flatMap(Collection::stream)
            .filter(CapabilityProperty::isMandatory)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryIvimPropertyNames = Stream.of(
            commonCapabilityProperties).flatMap(Collection::stream)
            .filter(CapabilityProperty::isMandatory)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatorySpatemMapemPropertyNames = Stream.of(
            commonCapabilityProperties).flatMap(Collection::stream)
            .filter(CapabilityProperty::isMandatory)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> filterableSpatemMapemPropertyNames = Stream.of(
            commonCapabilityProperties,
            spatemMapemCapabilityProperties).flatMap(Collection::stream)
            .filter(CapabilityProperty::isFilterable)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatorySremSsemPropertyNames = Stream.of(
            commonCapabilityProperties).flatMap(Collection::stream)
            .filter(CapabilityProperty::isMandatory)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryCamPropertyNames = Stream.of(
            commonCapabilityProperties).flatMap(Collection::stream)
            .filter(CapabilityProperty::isMandatory)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<CapabilityProperty> allProperties = Stream.of(
            commonCapabilityProperties,
            spatemMapemCapabilityProperties).flatMap(Collection::stream)
            .collect(Collectors.toSet());


    public static Set<String> nonFilterablePropertyNames = Stream.of(
            allProperties).flatMap(Collection::stream)
            .filter(p -> !p.isFilterable())
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    private String name;
    private boolean mandatory;
    private boolean filterable;
    private final CapabilityPropertyType propertyType;

    private CapabilityProperty(String name, boolean mandatory, boolean filterable, CapabilityPropertyType propertyType) {
        this.name = name;
        this.mandatory = mandatory;
        this.filterable = filterable;
        this.propertyType = propertyType;
    }

    //TODO: implement getProperty if needed
    //TODO: implement getCapabilityPropertyType if needed

    public String getName() {
        return name;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean isOptional() {
        return !mandatory;
    }

    public boolean isFilterable() {
        return filterable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapabilityProperty)) return false;
        CapabilityProperty that = (CapabilityProperty) o;
        return mandatory == that.mandatory &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, mandatory);
    }

    @Override
    public String toString() {
        return "CapabilityProperty{" +
                "name='" + name + '\'' +
                ", mandatory=" + mandatory +
                ", propertyType=" + propertyType +
                '}';
    }
}
