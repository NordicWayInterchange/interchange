package no.vegvesen.ixn.properties;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapabilityProperty {

    public static final CapabilityProperty MESSAGE_TYPE = new CapabilityProperty("messageType");
    public static final CapabilityProperty PUBLISHER_ID = new CapabilityProperty("publisherId");
    public static final CapabilityProperty PUBLICATION_ID = new CapabilityProperty("publicationId");
    public static final CapabilityProperty ORIGINATING_COUNTRY = new CapabilityProperty("originatingCountry");
    public static final CapabilityProperty PROTOCOL_VERSION = new CapabilityProperty("protocolVersion");
    public static final CapabilityProperty QUAD_TREE = new CapabilityProperty("quadTree");


    public static final List<CapabilityProperty> commonCapabilityProperties = Arrays.asList(
            MESSAGE_TYPE,
            PUBLISHER_ID,
            PUBLICATION_ID,
            ORIGINATING_COUNTRY,
            PROTOCOL_VERSION,
            QUAD_TREE
    );

    //publisherName
    public static final CapabilityProperty PUBLICATION_TYPE = new CapabilityProperty("publicationType");

    public static final List<CapabilityProperty> datex2CapabilityProperties = Collections.singletonList(
            PUBLICATION_TYPE
    );

    public static final CapabilityProperty CAUSE_CODE = new CapabilityProperty("causeCode");

    public static final List<CapabilityProperty> denmCapabilityProperties = Collections.singletonList(
            CAUSE_CODE
    );

    public static Set<String> mandatoryDatex2PropertyNames = Stream.of(
                    commonCapabilityProperties,
                    datex2CapabilityProperties
            ).flatMap(Collection::stream)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryDenmPropertyNames = Stream.of(
                    commonCapabilityProperties,
                    denmCapabilityProperties
            ).flatMap(Collection::stream)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryIvimPropertyNames = Stream.of(
                    commonCapabilityProperties).flatMap(Collection::stream)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatorySpatemMapemPropertyNames = Stream.of(
                    commonCapabilityProperties).flatMap(Collection::stream)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatorySremSsemPropertyNames = Stream.of(
                    commonCapabilityProperties).flatMap(Collection::stream)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryCamPropertyNames = Stream.of(
                    commonCapabilityProperties).flatMap(Collection::stream)
            .map(CapabilityProperty::getName)
            .collect(Collectors.toSet());

    public static Set<CapabilityProperty> allProperties = Stream.of(
                    commonCapabilityProperties,
                    datex2CapabilityProperties,
                    denmCapabilityProperties
            ).flatMap(Collection::stream)
            .collect(Collectors.toSet());

    private String name;

    private CapabilityProperty(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapabilityProperty)) return false;
        CapabilityProperty that = (CapabilityProperty) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CapabilityProperty{" +
                "name='" + name + '\'' +
                '}';
    }
}