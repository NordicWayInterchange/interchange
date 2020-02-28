package no.vegvesen.ixn.properties;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"WeakerAccess", "ArraysAsListWithZeroOrOneArgument"})
public class MessageProperty {

	public static final MessageProperty MESSAGE_TYPE = new MessageProperty("messageType", true, true, MessagePropertyType.STRING);
	public static final MessageProperty QUAD_TREE = new MessageProperty("quadTree", false, true, MessagePropertyType.STRING_ARRAY);
	public static final MessageProperty USER_ID = new MessageProperty("JMSXUserID", true, true, MessagePropertyType.STRING);
	public static final MessageProperty ORIGINATING_COUNTRY = new MessageProperty("originatingCountry", true, true, MessagePropertyType.STRING);

	public static final MessageProperty PUBLISHER_ID = new MessageProperty("publisherId", false, true, MessagePropertyType.STRING);
	public static final MessageProperty PUBLISHER_NAME = new MessageProperty("publisherName", true, true, MessagePropertyType.STRING);
	public static final MessageProperty LATITUDE = new MessageProperty("latitude", true, false, MessagePropertyType.STRING);
	public static final MessageProperty LONGITUDE = new MessageProperty("longitude", true, false, MessagePropertyType.STRING);
	public static final MessageProperty PROTOCOL_VERSION = new MessageProperty("protocolVersion", true, true, MessagePropertyType.STRING);
	public static final MessageProperty CONTENT_TYPE = new MessageProperty("contentType", false, true, MessagePropertyType.STRING);
	public static final MessageProperty TIMESTAMP = new MessageProperty("timestamp", false, false, MessagePropertyType.STRING);
	public static final List<MessageProperty> commonApplicationProperties = Arrays.asList(
			MESSAGE_TYPE,
			QUAD_TREE,
			PUBLISHER_ID,
			PUBLISHER_NAME,
			ORIGINATING_COUNTRY,
			PROTOCOL_VERSION,
			CONTENT_TYPE,
			LATITUDE,
			LONGITUDE,
			TIMESTAMP,
			new MessageProperty("relation", false, true, MessagePropertyType.STRING)
	);


	public static final MessageProperty PUBLICATION_TYPE = new MessageProperty("publicationType", true, true, MessagePropertyType.STRING);
	public static final MessageProperty PUBLICATION_SUB_TYPE = new MessageProperty("publicationSubType", false, true, MessagePropertyType.STRING_ARRAY);
	public static final List<MessageProperty> datex2ApplicationProperties = Arrays.asList(
			PUBLICATION_TYPE,
			PUBLICATION_SUB_TYPE
	);

	public static final MessageProperty SERVICE_TYPE = new MessageProperty("serviceType", false, true, MessagePropertyType.STRING);
	public static final List<MessageProperty> itsG5ApplicationProperties = Arrays.asList(
			SERVICE_TYPE
	);

	public static final MessageProperty CAUSE_CODE = new MessageProperty("causeCode", true, true, MessagePropertyType.STRING);
	public static final MessageProperty SUB_CAUSE_CODE = new MessageProperty("subCauseCode", false, true, MessagePropertyType.STRING);
	public static final List<MessageProperty> denmApplicationProperties = Arrays.asList(
			CAUSE_CODE,
			SUB_CAUSE_CODE
	);

	public static final MessageProperty IVI_TYPE = new MessageProperty("iviType", false, true, MessagePropertyType.INTEGER);
	public static final MessageProperty PICTOGRAM_CATEGORY_CODE = new MessageProperty("pictogramCategoryCode", false, true, MessagePropertyType.INTEGER_ARRAY);
	public static final List<MessageProperty> iviApplicationProperties = Arrays.asList(
			IVI_TYPE,
			PICTOGRAM_CATEGORY_CODE,
			new MessageProperty("iviContainer", false, false, MessagePropertyType.STRING)
	);

	public static Set<String> mandatoryDatex2PropertyNames = Stream.of(
			commonApplicationProperties,
			datex2ApplicationProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isMandatory)
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	public static Set<String> mandatoryDenmPropertyNames = Stream.of(
			commonApplicationProperties,
			itsG5ApplicationProperties,
			denmApplicationProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isMandatory)
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	public static Set<String> mandatoryIviPropertyNames = Stream.of(
			commonApplicationProperties,
			itsG5ApplicationProperties,
			iviApplicationProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isMandatory)
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	public static Set<MessageProperty> allProperties = Stream.of(
			commonApplicationProperties,
			datex2ApplicationProperties,
			itsG5ApplicationProperties,
			denmApplicationProperties,
			iviApplicationProperties).flatMap(Collection::stream)
			.collect(Collectors.toSet());

	public static Set<MessageProperty> filterableProperties = Stream.of(
			allProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isFilterable)
			.collect(Collectors.toSet());

	public static Set<String> nonFilterablePropertyNames = Stream.of(
			allProperties).flatMap(Collection::stream)
			.filter(messageProperty -> !messageProperty.isFilterable())
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	private String name;
	private boolean mandatory;
	private boolean filterable;
	private final MessagePropertyType messagePropertyType;

	private MessageProperty(String name, boolean mandatory, boolean filterable, MessagePropertyType propertyType) {
		this.name = name;
		this.mandatory = mandatory;
		this.filterable = filterable;
		this.messagePropertyType = propertyType;
	}

	public static MessageProperty getProperty(String key) {
		for (MessageProperty property : allProperties) {
			if (property.name.equals(key)) {
				return property;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public boolean isFilterable() {
		return filterable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MessageProperty that = (MessageProperty) o;
		return mandatory == that.mandatory &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, mandatory);
	}

	public MessagePropertyType getMessagePropertyType() {
		return messagePropertyType;
	}
}
