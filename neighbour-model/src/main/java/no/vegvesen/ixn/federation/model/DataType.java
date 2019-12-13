package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.DataTypeI;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeI;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "data_types")
public class DataType implements DataTypeI, Datex2DataTypeI{

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dat_generator")
	@SequenceGenerator(name="dat_generator", sequenceName = "dat_seq")
	@Column(name="dat_id")
	private Integer data_id;

	private String originatingCountry;
	private String messageType;
	//Datex2
	private String publicationType;
	private String publicationSubTypes;


	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public DataType(){}

	public DataType(String messageType, String originatingCountry) {
		this.messageType = messageType;
		this.originatingCountry = originatingCountry;
	}

	//Datex2
	public DataType(String messageType, String originatingCountry, String publicationType, String publicationSubTypes) {
		this.messageType = messageType;
		this.originatingCountry = originatingCountry;
		this.publicationType = publicationType;
		this.publicationSubTypes = publicationSubTypes;
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
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DataType dataType = (DataType) o;

		if (originatingCountry != null ? !originatingCountry.equals(dataType.originatingCountry) : dataType.originatingCountry != null)
			return false;
		if (!messageType.equals(dataType.messageType)) return false;
		if (publicationType != null ? !publicationType.equals(dataType.publicationType) : dataType.publicationType != null)
			return false;
		return publicationSubTypes != null ? publicationSubTypes.equals(dataType.publicationSubTypes) : dataType.publicationSubTypes == null;
	}

	@Override
	public int hashCode() {
		int result = originatingCountry != null ? originatingCountry.hashCode() : 0;
		result = 31 * result + messageType.hashCode();
		result = 31 * result + (publicationType != null ? publicationType.hashCode() : 0);
		result = 31 * result + (publicationSubTypes != null ? publicationSubTypes.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "DataType{" +
				"data_id=" + data_id +
				", originatingCountry='" + originatingCountry + '\'' +
				", messageType='" + messageType + '\'' +
				", publicationType='" + publicationType + '\'' +
				", publicationSubType='" + publicationSubTypes + '\'' +
				", lastUpdated=" + lastUpdated +
				'}';
	}

	@Override
	public String getPublicationType() {
		return this.publicationType;

	}

	@Override
	public void setPublicationType(String publicationType) {
		this.publicationType = publicationType;
	}

	public String getPublicationSubTypes() {
		return publicationSubTypes;
	}

	public void setPublicationSubTypes(String publicationSubTypes) {
		this.publicationSubTypes = publicationSubTypes;
	}
}
