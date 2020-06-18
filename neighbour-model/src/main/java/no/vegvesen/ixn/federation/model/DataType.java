package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.properties.MessagePropertyType;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "data_types")
public class DataType {

	private static Logger logger = LoggerFactory.getLogger(DataType.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dat_seq")
	@Column(name = "dat_id")
	private Integer data_id;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "data_type_values", joinColumns = @JoinColumn(name = "dat_id"))
	@MapKeyColumn(name = "property")
	@Column(name = "value")
	private Map<String, String> values = new HashMap<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public DataType() {
	}

	public DataType(Integer id, String key, String value) {
		this.data_id = id;
		this.values.put(key, value);
	}


	public DataType(Map<String, String> values) {
		this.values = new HashMap<>(values);
	}

	@SuppressWarnings("WeakerAccess")
	public boolean isContainedInSet(Set<DataType> capabilities) {
		for (DataType other : capabilities) {
			if (this.equals(other)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataType dataType = (DataType) o;

		return Objects.equals(values, dataType.values);
	}

	@Override
	public int hashCode() {
		return values != null ? values.hashCode() : 0;
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

	public Integer getPropertyValueAsInteger(MessageProperty messageProperty) {
		String stringIntegerValue = getPropertyValue(messageProperty);
		if (stringIntegerValue != null) {
			return Integer.parseInt(stringIntegerValue);
		}
		return null;
	}

	public Set<Integer> getPropertyValueAsIntegerSet(MessageProperty messageProperty) {
		List<String> intArrayValues = getPropertyValueAsList(messageProperty);
		return Stream.of(intArrayValues).flatMap(Collection::stream)
				.map(n -> {
					try {
						return Integer.parseInt(n);
					} catch (NumberFormatException e) {
						logger.error("Cannot parse messageProperty [{}] value as integer [{}]", messageProperty.getName(), n);
						return null;
					}
				}).filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	public Integer getData_id() {
		return data_id;
	}

	public boolean matches(DataType other) {
		Set<String> thisKeys = values.keySet();
		Set<String> otherKeys = other.values.keySet();
		Set<String> commonKeys = intersection(thisKeys, otherKeys);
		for (String key : commonKeys) {
			boolean match = matches(key, this.getValues().get(key), other.getValues().get(key));
			if (!match) {
				return false;
			}
		}
		return !commonKeys.isEmpty();
	}

	private static boolean matches(String key, String v1, String v2) {
		if (MessageProperty.QUAD_TREE.getName().equals(key)) {
			return matchesQuadTree(v1, v2);
		}
		MessageProperty messageProperty = MessageProperty.getProperty(key);
		if (messageProperty == null) {
			logger.error("No such property found when matching DataType {}", key);
			throw new RuntimeException("No such property " + key);
		}
		boolean matches;
		if (messageProperty.getMessagePropertyType().isArray()) {
			matches = matchesArray(v1, v2);
		} else {
			matches = matchesSingleValue(v1, v2);
		}
		logger.debug("Matching property {} values {} / {} - matches {}", key, v1, v2, matches);
		return matches;
	}

	static boolean matchesArray(String v1, String v2) {
		Set<String> v1values = new HashSet<>(getListElements(v1));
		Set<String> v2values = new HashSet<>(getListElements(v2));
		return !intersection(v1values, v2values).isEmpty();
	}

	private static Set<String> intersection(Set<String> v1values, Set<String> v2values) {
		Set<String> intersection = new HashSet<>(v1values);
		intersection.retainAll(v2values);
		return intersection;
	}

	static boolean matchesSingleValue(String v1, String v2) {
		return (v1 == null && v2 == null) || v1 != null && v1.equals(v2);
	}

	static boolean matchesQuadTree(String v1, String v2) {
		Set<String> v1values = new HashSet<>(getListElements(v1));
		Set<String> v2values = new HashSet<>(getListElements(v2));
		if (v1values.isEmpty() || v2values.isEmpty()) {
			return true;
		}
		for (String v1Value : v1values) {
			for (String v2Value : v2values) {
				if (v1Value.startsWith(v2Value) || v2Value.startsWith(v1Value)) {
					return true;
				}
			}
		}
		return false;
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

	public static Subscription toSubscription(DataType dataType) {
		return new Subscription(dataType.toSelector(), SubscriptionStatus.ACCEPTED);
	}

	@Override
	public String toString() {
		return "DataType{" +
				"data_id=" + data_id +
				", values=" + values +
				", lastUpdated=" + lastUpdated +
				'}';
	}
}


