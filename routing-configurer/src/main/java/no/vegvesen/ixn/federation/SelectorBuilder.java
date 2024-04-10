package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.properties.MessagePropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * NOTE this class is a selector builder, but is retrofitted from an earlier model object.
 * It is not used in production code, as the selector building is not properly tested.
 */
public class SelectorBuilder {

	private static Logger logger = LoggerFactory.getLogger(SelectorBuilder.class);
	private Map<String, String> values = new HashMap<>();

	public SelectorBuilder() {

	}

	private String getPropertyValue(MessageProperty property) {
		return this.values.get(property.getName());
	}

	private List<String> getPropertyValueAsList(MessageProperty property) {
		String commaSeparatedString = getPropertyValue(property);
		return getListElements(commaSeparatedString);
	}

	private static List<String> getListElements(String commaSeparatedString) {
		if (commaSeparatedString == null) {
			return Collections.emptyList();
		}
		List<String> elements = Arrays.asList(commaSeparatedString.split(","));
		return elements.stream()
				.filter(s -> s.length() > 0)
				.collect(Collectors.toList());
	}

	private Set<String> getPropertyValueAsSet(MessageProperty messageProperty) {
		List<String> propertyValueAsList = getPropertyValueAsList(messageProperty);
		if (propertyValueAsList.isEmpty()) {
			return Collections.emptySet();
		}
		return new HashSet<>(propertyValueAsList);
	}

	public String toSelector() {
		Set<String> selectorElements = new HashSet<>();
		for (String key : values.keySet()) {
			MessageProperty property = MessageProperty.getProperty(key);
			assert property != null;
			switch (property.getMessagePropertyType()) {
				case INTEGER:
				case STRING:
					//note that we might have multiple values in the cap, but only a single value in the headers
					addSelector(selectorElements, arraySelector(property));
					break;
				case STRING_ARRAY:
				case INTEGER_ARRAY:
					addSelector(selectorElements, arraySelector(property));
			}
		}
		return String.join(" AND ", selectorElements);
	}

	private void addSelector(Set<String> selectorElements, String s) {
		if (s != null && s.length() > 0) {
			selectorElements.add(s);
		}
	}

	private String singleValueSelector(MessageProperty property) {
		String propertyValue = getPropertyValue(property);
		if (propertyValue == null) {
			return null;
		}
		return oneSelector(property, propertyValue);
	}

	private String oneSelector(MessageProperty property, String propertyValue) {
		if (MessagePropertyType.INTEGER == property.getMessagePropertyType() ||
				MessagePropertyType.INTEGER_ARRAY == property.getMessagePropertyType()) {
			return String.format("%s = %s", property.getName(), propertyValue);
		} else if (MessageProperty.QUAD_TREE == property) {
			return String.format("%s like '%%,%s%%'", property.getName(), propertyValue);
		} else if (MessagePropertyType.STRING_ARRAY == property.getMessagePropertyType()) {
			return String.format("%s like '%%,%s,%%'",property.getName(),propertyValue);
		}
		return String.format("%s = '%s'", property.getName(), propertyValue);
	}

	private String arraySelector(MessageProperty property) {
		Set<String> values = getPropertyValueAsSet(property);
		if (values.isEmpty()) {
			return null;
		} else {
			Set<String> arraySelectors = new HashSet<>();
			for (String value : values) {
				addSelector(arraySelectors, oneSelector(property, value));
			}
			return String.format("(%s)", String.join(" OR ", arraySelectors));
		}
	}

	public SelectorBuilder originatingCountry(String country) {
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(),country);
		return this;
	}

	public SelectorBuilder messageType(String messageType) {
		values.put(MessageProperty.MESSAGE_TYPE.getName(),messageType);
		return this;
	}

	/**
	 * Adds string form of quadTree, usually from message headers
	 * @param quadTree
	 * @return
	 */
	public SelectorBuilder quadTree(String quadTree) {
		values.put(MessageProperty.QUAD_TREE.getName(),quadTree);
		return this;
	}

	/**
	 * Adds a set of individual quadtrees, ususally from a Capability
	 * @param quadTrees
	 * @return
	 */
	public SelectorBuilder quadTree(Set<String> quadTrees) {
		values.put(MessageProperty.QUAD_TREE.getName(), String.join(",",quadTrees));
		return this;
	}

	public SelectorBuilder publisherId(String publisherId) {
		values.put(MessageProperty.PUBLISHER_ID.getName(), publisherId);
		return this;
	}

	public SelectorBuilder publicationId(String publicationId) {
		values.put(MessageProperty.PUBLICATION_ID.getName(), publicationId);
		return this;
	}

	public SelectorBuilder shardId(String shardId) {
		values.put(MessageProperty.SHARD_ID.getName(), shardId);
		return this;
	}

	public SelectorBuilder protocolVersion(String protocolVersion) {
		values.put(MessageProperty.PROTOCOL_VERSION.getName(), protocolVersion);
		return this;
	}

	public SelectorBuilder iviTypes(Set<String> iviTypes) {
		values.put(MessageProperty.IVI_TYPE.getName(), String.join(",",iviTypes));
		return this;
	}

	public SelectorBuilder causeCode(Set<Integer> causeCodes) {
		String strings = causeCodes.stream()
				.map(n -> String.valueOf(n))
				.collect(Collectors.joining(","));
		values.put(MessageProperty.CAUSE_CODE.getName(), strings);
		return this;
	}

	public SelectorBuilder publicationTypes(String publicationType) {
		values.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
		return this;
	}
}


