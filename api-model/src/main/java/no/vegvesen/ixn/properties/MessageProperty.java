package no.vegvesen.ixn.properties;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"WeakerAccess"})
public class MessageProperty {

	public static final MessageProperty MESSAGE_TYPE = new MessageProperty("messageType", true, true, MessagePropertyType.STRING);
	public static final MessageProperty QUAD_TREE = new MessageProperty("quadTree", true, true, MessagePropertyType.STRING_ARRAY);

	/* Is this to be removed? */
	public static final MessageProperty USER_ID = new MessageProperty("JMSXUserID", true, true, MessagePropertyType.STRING);

	public static final MessageProperty PUBLICATION_ID = new MessageProperty("publicationId", true, true, MessagePropertyType.STRING);

	public static final MessageProperty ORIGINATING_COUNTRY = new MessageProperty("originatingCountry", true, true, MessagePropertyType.STRING);
	public static final MessageProperty PUBLISHER_ID = new MessageProperty("publisherId", true, true, MessagePropertyType.STRING);
	public static final MessageProperty LATITUDE = new MessageProperty("latitude", false, false, MessagePropertyType.DOUBLE);
	public static final MessageProperty LONGITUDE = new MessageProperty("longitude", false, false, MessagePropertyType.DOUBLE);
	public static final MessageProperty PROTOCOL_VERSION = new MessageProperty("protocolVersion", true, true, MessagePropertyType.STRING);
	//public static final MessageProperty CONTENT_TYPE = new MessageProperty("contentType", false, true, MessagePropertyType.STRING);
	public static final MessageProperty TIMESTAMP = new MessageProperty("timestamp", false, false, MessagePropertyType.STRING);
	//TODO: serviceType is a comma separated list of Strings
	public static final MessageProperty SERVICE_TYPE = new MessageProperty("serviceType", false, true, MessagePropertyType.STRING_ARRAY);

	public static final MessageProperty SHARD_ID = new MessageProperty("shardId", false, true, MessagePropertyType.INTEGER);

	public static final MessageProperty SHARD_COUNT = new MessageProperty("shardCount", false, true, MessagePropertyType.INTEGER);

	public static final MessageProperty BASELINE_VERSION = new MessageProperty("baselineVersion", false, true, MessagePropertyType.STRING);
	public static final List<MessageProperty> commonApplicationProperties = Arrays.asList(
			MESSAGE_TYPE,
			QUAD_TREE,
			PUBLICATION_ID,
			PUBLISHER_ID,
			ORIGINATING_COUNTRY,
			PROTOCOL_VERSION,
			SERVICE_TYPE,
			LATITUDE,
			LONGITUDE,
			SHARD_ID,
			SHARD_COUNT,
			BASELINE_VERSION
	);

	public static final MessageProperty PUBLISHER_NAME = new MessageProperty("publisherName", true, true, MessagePropertyType.STRING);
	public static final MessageProperty PUBLICATION_TYPE = new MessageProperty("publicationType", true, true, MessagePropertyType.STRING);
	public static final MessageProperty PUBLICATION_SUB_TYPE = new MessageProperty("publicationSubType", false, true, MessagePropertyType.STRING_ARRAY);
	public static final List<MessageProperty> datex2ApplicationProperties = Arrays.asList(
			PUBLICATION_TYPE,
			PUBLICATION_SUB_TYPE,
			PUBLISHER_NAME
	);

	public static final List<MessageProperty> itsG5ApplicationProperties = Arrays.asList(
			SERVICE_TYPE
	);

	//TODO: causeCode and subCauseCode is changed to Integer
	public static final MessageProperty CAUSE_CODE = new MessageProperty("causeCode", true, true, MessagePropertyType.INTEGER);
	public static final MessageProperty SUB_CAUSE_CODE = new MessageProperty("subCauseCode", true, true, MessagePropertyType.INTEGER);
	public static final List<MessageProperty> denmApplicationProperties = Arrays.asList(
			CAUSE_CODE,
			SUB_CAUSE_CODE
	);

	public static final MessageProperty IVI_TYPE = new MessageProperty("iviType", false, true, MessagePropertyType.STRING_ARRAY);
	public static final MessageProperty PICTOGRAM_CATEGORY_CODE = new MessageProperty("pictogramCategoryCode", false, true, MessagePropertyType.INTEGER_ARRAY);

	/* TODO This is specified as String, but obviously has some string-array properties. Double check that this is not a mistake in spec */
	public static final MessageProperty IVI_CONTAINER = new MessageProperty("iviContainer", false, false, MessagePropertyType.STRING_ARRAY);
	public static final List<MessageProperty> ivimApplicationProperties = Arrays.asList(
			IVI_TYPE,
			PICTOGRAM_CATEGORY_CODE,
			IVI_CONTAINER
	);

	public static final MessageProperty IDS = new MessageProperty("id", false, true, MessagePropertyType.STRING_ARRAY);
	public static final MessageProperty NAME = new MessageProperty("name", false, true, MessagePropertyType.STRING_ARRAY);

	public static final List<MessageProperty> spatemMapemApplicationProperties = Arrays.asList(
			IDS,
			NAME
	);

	public static final List<MessageProperty> sremSsemApplicationProperties = Arrays.asList(
			IDS
	);

	public static final MessageProperty STATION_TYPE = new MessageProperty("stationType", true, true, MessagePropertyType.INTEGER);
	public static final MessageProperty VEHICLE_ROLE = new MessageProperty("vehicleRole", false, true, MessagePropertyType.INTEGER);

	public static final List<MessageProperty> camApplicationProperties = Arrays.asList(
			STATION_TYPE,
			VEHICLE_ROLE
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

	public static Set<String> mandatoryIvimPropertyNames = Stream.of(
			commonApplicationProperties,
			itsG5ApplicationProperties,
			ivimApplicationProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isMandatory)
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	public static Set<String> mandatorySpatemMapemPropertyNames = Stream.of(
			commonApplicationProperties,
			itsG5ApplicationProperties,
			spatemMapemApplicationProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isMandatory)
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	public static Set<String> mandatorySremSsemPropertyNames = Stream.of(
			commonApplicationProperties,
			itsG5ApplicationProperties,
			sremSsemApplicationProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isMandatory)
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	public static Set<String> mandatoryCamPropertyNames = Stream.of(
			commonApplicationProperties,
			itsG5ApplicationProperties,
			camApplicationProperties).flatMap(Collection::stream)
			.filter(MessageProperty::isMandatory)
			.map(MessageProperty::getName)
			.collect(Collectors.toSet());

	public static Set<MessageProperty> allProperties = Stream.of(
			commonApplicationProperties,
			datex2ApplicationProperties,
			itsG5ApplicationProperties,
			denmApplicationProperties,
			ivimApplicationProperties,
					spatemMapemApplicationProperties,
					sremSsemApplicationProperties,
					camApplicationProperties).flatMap(Collection::stream)
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

	public boolean isOptional() {
		return !mandatory;
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

	@Override
	public String toString() {
		return "MessageProperty{" +
				"name='" + name + '\'' +
				", mandatory=" + mandatory +
				", filterable=" + filterable +
				", messagePropertyType=" + messagePropertyType +
				'}';
	}
}
