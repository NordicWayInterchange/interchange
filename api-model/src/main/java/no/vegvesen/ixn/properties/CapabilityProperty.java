package no.vegvesen.ixn.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CapabilityProperty {

    public static final CapabilityProperty MESSAGE_TYPE = new CapabilityProperty("messageType", true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty PUBLISHER_ID = new CapabilityProperty("publisherId", true, CapabilityPropertyType.STRING);
    //public static final CapabilityProperty PUBLICATION_ID = new CapabilityProperty("publicationId", true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty ORIGINATING_COUNTRY = new CapabilityProperty("originatingCountry", true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty PROTOCOL_VERSION = new CapabilityProperty("protocolVersion", true, CapabilityPropertyType.STRING);
    public static final CapabilityProperty QUAD_TREE = new CapabilityProperty("quadTree", true, CapabilityPropertyType.STRING_ARRAY);
    //public static final CapabilityProperty INFO_URL = new CapabilityProperty("infoUrl", false, CapabilityPropertyType.STRING);

    public static final List<CapabilityProperty> commonCapabilityProperties = Arrays.asList(
            MESSAGE_TYPE,
            PUBLISHER_ID,
            ORIGINATING_COUNTRY,
            PROTOCOL_VERSION,
            QUAD_TREE
    );

    public static final CapabilityProperty PUBLICATION_TYPE = new CapabilityProperty("publicationType", false, CapabilityPropertyType.STRING_ARRAY);

    public static final List<CapabilityProperty> datex2CapabilityProperties = Arrays.asList(
            PUBLICATION_TYPE
    );

    public static final CapabilityProperty CAUSE_CODE = new CapabilityProperty("causeCode", true, CapabilityPropertyType.STRING_ARRAY);

    public static final List<CapabilityProperty> denmCapabilityProperties = Arrays.asList(
            CAUSE_CODE
    );

    public static final CapabilityProperty IVI_TYPE = new CapabilityProperty("iviType", false, CapabilityPropertyType.STRING_ARRAY);

    public static final List<CapabilityProperty> ivimCapabilityProperties = Arrays.asList(
            IVI_TYPE
    );

    private String name;
    private boolean mandatory;
    private final CapabilityPropertyType propertyType;

    private CapabilityProperty(String name, boolean mandatory, CapabilityPropertyType propertyType) {
        this.name = name;
        this.mandatory = mandatory;
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
