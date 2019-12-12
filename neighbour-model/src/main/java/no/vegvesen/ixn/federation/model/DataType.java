package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.DataTypeI;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "data_types")
public class DataType implements DataTypeI {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dat_generator")
	@SequenceGenerator(name="dat_generator", sequenceName = "dat_seq")
	@Column(name="dat_id")
	private Integer data_id;

	private String originatingCountry;
	private String how;

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public DataType(){}

	public DataType(String how, String originatingCountry) {
		this.how = how;
		this.originatingCountry = originatingCountry;
	}

	@Override
	public String getOriginatingCountry() {
		return originatingCountry;
	}

	@Override
	public void setOriginatingCountry(String where) {
		this.originatingCountry = where;
	}

	@Override
	public String getHow() {
		return how;
	}

	@Override
	public void setHow(String how) {
		this.how = how;
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
		if(o == null) return false;
		if (this == o) return true;
		if (!(o instanceof DataType)) return false;

		DataType dataType = (DataType) o;

		if (!originatingCountry.equals(dataType.originatingCountry)) return false;
		return how.equals(dataType.how);
	}

	@Override
	public int hashCode() {
		int result = originatingCountry.hashCode();
		result = 31 * result + how.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "DataType{" +
				"data_id=" + data_id +
				", originatingCountry='" + originatingCountry + '\'' +
				", how='" + how + '\'' +
				", lastUpdated=" + lastUpdated +
				'}';
	}
}
