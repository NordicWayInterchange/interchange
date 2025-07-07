package no.vegvesen.ixn.federation.selector;

import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.properties.MessagePropertyType;

import java.util.*;
import java.util.stream.Collectors;


public class SelectorBuilder {

	private final Map<String, String> values = new HashMap<>();

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
				.filter(s -> !s.isEmpty())
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
		if (s != null && !s.isEmpty()) {
			selectorElements.add(s);
		}
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
	 * @param quadTree the quadTree
	 * @return SelectorBuilder for further building
	 */
	public SelectorBuilder quadTree(String quadTree) {
		values.put(MessageProperty.QUAD_TREE.getName(),quadTree);
		return this;
	}

	/**
	 * Adds a set of individual quadtrees, ususally from a Capability
	 * @param quadTrees the quadTrees to add
	 * @return SelectorBuilder for further building
	 */
	public SelectorBuilder quadTree(List<String> quadTrees) {
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

	public SelectorBuilder causeCode(List<Integer> causeCodes) {
		String strings = causeCodes.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
		values.put(MessageProperty.CAUSE_CODE.getName(), strings);
		return this;
	}

	public SelectorBuilder publicationTypes(String publicationType) {
		values.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
		return this;
	}
}


