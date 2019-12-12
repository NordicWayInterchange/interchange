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
	private String messageType;

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public DataType(){}

	public DataType(String messageType, String originatingCountry) {
		this.messageType = messageType;
		this.originatingCountry = originatingCountry;
	}

	@Override
	public String getOriginatingCountry() {
		return originatingCountry;
	}

	@Override
	public void setOriginatingCountry(String originatingCountry) {
		this.originatingCountry = originatingCountry;
	}

	@Override
	public String getMessageType() {
		return messageType;
	}

	@Override
	public void setMessageType(String messageType) {
		this.messageType = messageType;
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
		return messageType.equals(dataType.messageType);
	}

	@Override
	public int hashCode() {
		int result = originatingCountry.hashCode();
		result = 31 * result + messageType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "DataType{" +
				"data_id=" + data_id +
				", originatingCountry='" + originatingCountry + '\'' +
				", messageType='" + messageType + '\'' +
				", lastUpdated=" + lastUpdated +
				'}';
	}
}
