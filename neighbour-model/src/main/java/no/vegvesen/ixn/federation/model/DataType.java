package no.vegvesen.ixn.federation.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.properties.MessageProperty;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dat_generator")
	@SequenceGenerator(name = "dat_generator", sequenceName = "dat_seq")
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
		return Stream.of(Lists.newArrayList(commaSeparatedString.split(","))).flatMap(Collection::stream)
				.filter(s -> s.length() > 0)
				.collect(Collectors.toList());
	}

	public Set<String> getPropertyValueAsSet(MessageProperty messageProperty) {
		List<String> propertyValueAsList = getPropertyValueAsList(messageProperty);
		if (propertyValueAsList.isEmpty()) {
			return Collections.emptySet();
		}
		return Sets.newHashSet(propertyValueAsList);
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
		Sets.SetView<String> commonKeys = Sets.intersection(thisKeys, otherKeys);
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
		if (messageProperty.isArray()) {
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
		return !Sets.intersection(v1values, v2values).isEmpty();
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
		StringBuilder selector = new StringBuilder();
		Iterator<String> iterator = values.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			selector.append(String.format("%s = '%s'", key, values.get(key)));
			if (iterator.hasNext()) {
				selector.append(" AND ");
			}
		}
		return selector.toString();
	}

	public static Subscription toSubscription(DataType dataType) {
		return new Subscription(dataType.toSelector(), SubscriptionStatus.REQUESTED);
	}
}


