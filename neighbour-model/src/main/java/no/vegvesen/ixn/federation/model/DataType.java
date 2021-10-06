package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.properties.MessagePropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DataType {

	private static Logger logger = LoggerFactory.getLogger(DataType.class);
	private Map<String, String> values = new HashMap<>();

	public DataType(Map<String, String> values) {
		this.values = new HashMap<>(values);
	}

	public DataType() {

	}

	public Map<String, String> getValues() {
		return values;
	}

	public void setValues(Map<String, String> values) {
		this.values = values;
	}

	public String getPropertyValue(MessageProperty property) {
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

	public Set<String> getPropertyValueAsSet(MessageProperty messageProperty) {
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
					addSelector(selectorElements, singleValueSelector(property));
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

	public DataType originatingCountry(String country) {
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(),country);
		return this;
	}

	public DataType messageType(String messageType) {
		values.put(MessageProperty.MESSAGE_TYPE.getName(),messageType);
		return this;
	}

	public DataType quadTree(String quadTree) {
		values.put(MessageProperty.QUAD_TREE.getName(),quadTree);
		return this;
	}
}


