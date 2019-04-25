package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;


@Entity
@Table(name = "capabilities")
public class Capabilities {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_generator")
	@SequenceGenerator(name="cap_generator", sequenceName = "cap_seq", allocationSize=50)
	@Column(name="cap_id")
	private Integer cap_id;

	public enum CapabilitiesStatus{UNKNOWN, KNOWN, FAILED, UNREACHABLE}

	@Enumerated(EnumType.STRING)
	private CapabilitiesStatus status;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id_dat", foreignKey = @ForeignKey(name="fk_dat_cap"))
	private Set<DataType> dataTypes;

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public Capabilities(){
	}

	public Capabilities(CapabilitiesStatus status, Set<DataType> capabilities) {
		this.status = status;
		this.dataTypes = capabilities;
	}

	public CapabilitiesStatus getStatus() {
		return status;
	}

	public void setStatus(CapabilitiesStatus status) {
		this.status = status;
	}

	public Set<DataType> getDataTypes() {
		return dataTypes;
	}

	public void setDataTypes(Set<DataType> capabilities) {
		this.dataTypes = capabilities;
	}

	@Override
	public String toString() {
		return "Capabilities{" +
				"cap_id=" + cap_id +
				", status=" + status +
				", dataTypes=" + dataTypes +
				'}';
	}
}
