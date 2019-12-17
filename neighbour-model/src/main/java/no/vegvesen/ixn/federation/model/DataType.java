package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.properties.MessageProperty;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "data_types")
public class DataType{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dat_generator")
	@SequenceGenerator(name="dat_generator", sequenceName = "dat_seq")
	@Column(name="dat_id")
	private Integer data_id;

	@ElementCollection
	private Map<String, String> values;

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

	public String[] getPropertyValueAsArray(MessageProperty property) {
		String commaSeparatedString = getPropertyValue(property);
		return commaSeparatedString == null ? null : commaSeparatedString
				.replaceAll("\\A,", "")
				.replaceAll(",\\z", "")
				.split(",");
	}
}
