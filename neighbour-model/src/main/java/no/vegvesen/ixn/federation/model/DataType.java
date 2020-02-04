package no.vegvesen.ixn.federation.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import no.vegvesen.ixn.properties.MessageProperty;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "data_types")
public class DataType{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dat_generator")
	@SequenceGenerator(name="dat_generator", sequenceName = "dat_seq")
	@Column(name="dat_id")
	private Integer data_id;

	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, String> values = new HashMap<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public DataType(){}

	public DataType(Map<String, String> values) {
		this.values = new HashMap<>(values);
	}

	@SuppressWarnings("WeakerAccess")
	public boolean isContainedInSet(Set<DataType> capabilities){
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

		return values != null ? values.equals(dataType.values) : dataType.values == null;
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
		String intArrayValues = getPropertyValue(messageProperty);
		return Stream.of(Lists.newArrayList(intArrayValues.split(","))).flatMap(Collection::stream)
				.filter(s -> s.length() > 0)
				.map(Integer::parseInt)
				.collect(Collectors.toSet());
	}

	public Integer getData_id() {
		return data_id;
	}
}
