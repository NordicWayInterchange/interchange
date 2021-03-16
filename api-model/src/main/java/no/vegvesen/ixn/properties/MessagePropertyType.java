package no.vegvesen.ixn.properties;

public enum MessagePropertyType {
	STRING(false), STRING_ARRAY(true), INTEGER(false), INTEGER_ARRAY(true), DOUBLE(false);

	public final boolean array;
	MessagePropertyType(boolean array) {
		this.array = array;
	}
	public boolean isArray() {
		return array;
	}
}