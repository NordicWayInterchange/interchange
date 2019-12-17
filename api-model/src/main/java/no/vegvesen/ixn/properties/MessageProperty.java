package no.vegvesen.ixn.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"WeakerAccess", "ArraysAsListWithZeroOrOneArgument"})
public class MessageProperty {

	public static final MessageProperty MESSAGE_TYPE = new MessageProperty("messageType", true, true);
    public static final MessageProperty QUAD_TREE = new MessageProperty("quadTree", false, false);
    public static final MessageProperty USER_ID = new MessageProperty("JMSXUserID", true, true);
	public static final MessageProperty ORIGINATING_COUNTRY = new MessageProperty("originatingCountry", true, true);

	public static final List<MessageProperty> commonApplicationProperties = Arrays.asList(
            MESSAGE_TYPE,
            QUAD_TREE,
            new MessageProperty("publisherId", false, true),
            new MessageProperty("publisherName", true, true),
			ORIGINATING_COUNTRY,
            new MessageProperty("protocolVersion", true, true),
            new MessageProperty("contentType", false, true),
            new MessageProperty("latitude", true, true),
            new MessageProperty("longitude", true, true),
            new MessageProperty("timestamp", false, true),
            new MessageProperty("relation", false, true)
    );


	public static final MessageProperty PUBLICATION_TYPE = new MessageProperty("publicationType", true, true);
	public static final MessageProperty PUBLICATION_SUB_TYPE = new MessageProperty("publicationSubType", false, true);
	public static final List<MessageProperty> datex2ApplicationProperties = Arrays.asList(
            PUBLICATION_TYPE,
			PUBLICATION_SUB_TYPE
    );

    public static final List<MessageProperty> itsG5ApplicationProperties = Arrays.asList(
            new MessageProperty("serviceType",false, true)
    );

    public static final List<MessageProperty> denmApplicationProperties = Arrays.asList(
            new MessageProperty("causeCode",true, true),
            new MessageProperty("subCauseCode",false, true)
    );

    public static final List<MessageProperty> iviApplicationProperties = Arrays.asList(
            new MessageProperty("IviType",false, true),
            new MessageProperty("pictogramCategoryCode",false, true),
            new MessageProperty("iviContainer",false, true)
    );

	public static Set<String> mandatoryDatex2PropertyNames = Stream.of(commonApplicationProperties,datex2ApplicationProperties)
            .flatMap(messageProperties -> messageProperties.stream())
            .filter(MessageProperty::isMandatory)
            .map(MessageProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryDenmPropertyNames = Stream.of(commonApplicationProperties,itsG5ApplicationProperties,denmApplicationProperties)
            .flatMap(messageProperties -> messageProperties.stream())
            .filter(MessageProperty::isMandatory)
            .map(MessageProperty::getName)
            .collect(Collectors.toSet());

    public static Set<String> mandatoryIviPropertyNames = Stream.of(commonApplicationProperties,itsG5ApplicationProperties,iviApplicationProperties)
            .flatMap(messageProperties -> messageProperties.stream())
            .filter(MessageProperty::isMandatory)
            .map(MessageProperty::getName)
            .collect(Collectors.toSet());


    public static Set<MessageProperty> allProperties = Stream.of(
            commonApplicationProperties,
            datex2ApplicationProperties,
            itsG5ApplicationProperties,
            denmApplicationProperties,
            iviApplicationProperties).flatMap(messageProperties -> messageProperties.stream())
            .collect(Collectors.toSet());

	public static Set<MessageProperty> filterableProperties = Stream.of(
			allProperties).flatMap(messageProperties -> messageProperties.stream())
			.filter(MessageProperty::isFilterable)
			.collect(Collectors.toSet());

    private String name;
    private boolean mandatory;
    private boolean filterable;

    private MessageProperty(String name, boolean mandatory, boolean filterable) {
        this.name = name;
        this.mandatory = mandatory;
		this.filterable = filterable;
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
}
