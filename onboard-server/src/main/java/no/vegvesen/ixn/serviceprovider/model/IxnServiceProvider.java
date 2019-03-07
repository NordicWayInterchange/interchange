package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.vegvesen.ixn.federation.model.DataType;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "service_providers")
public class IxnServiceProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sp_generator")
	@SequenceGenerator(name="sp_generator", sequenceName = "sp_seq", allocationSize=50)
	@Column(name="sp_id")
	private Integer id;

	@Column(unique = true)
	private String name;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "fk_sp_id")
	private Set<DataType> capabilities;

	public IxnServiceProvider() {
	}

	@JsonCreator
	public IxnServiceProvider(@JsonProperty("name") String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Set<DataType> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<DataType> capabilities) {
		this.capabilities = capabilities;
	}
}
