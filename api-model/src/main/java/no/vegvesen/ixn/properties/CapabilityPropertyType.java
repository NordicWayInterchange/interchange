package no.vegvesen.ixn.properties;

public enum CapabilityPropertyType {
    STRING(false), STRING_ARRAY(true), INTEGER(false), INTEGER_ARRAY(true), DOUBLE(false);

    public final boolean array;

    CapabilityPropertyType(boolean array) {
        this.array = array;
    }

    public boolean isArray() {
        return array;
    }
}
