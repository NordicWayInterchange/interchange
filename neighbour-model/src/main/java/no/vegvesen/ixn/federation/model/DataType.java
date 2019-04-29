package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "data_types")
public class DataType {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dat_generator")
	@SequenceGenerator(name="dat_generator", sequenceName = "dat_seq")
	@Column(name="dat_id")
	private Integer data_id;

	private String where1;
	private String how;
	private String what;

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public DataType(){}

	public DataType(String how, String where1, String what) {
		this.how = how;
		this.what = what;
		this.where1 = where1;
	}

	public String getWhere1() {
		return where1;
	}

	public void setWhere1(String where1) {
		this.where1 = where1;
	}

	public String getHow() {
		return how;
	}

	public void setHow(String how) {
		this.how = how;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	public boolean isContainedInSet(Set<DataType> capabilities){
		for (DataType other : capabilities) {
			if (this.getHow().equals(other.getHow()) && this.getWhat().equals(other.getWhat()) && this.getWhere1().equals(other.getWhere1())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "DataType{" +
				"data_id=" + data_id +
				", where1='" + where1 + '\'' +
				", how='" + how + '\'' +
				", what='" + what + '\'' +
				", lastUpdated=" + lastUpdated +
				'}';
	}
}
