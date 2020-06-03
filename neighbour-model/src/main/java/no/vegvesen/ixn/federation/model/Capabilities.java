package no.vegvesen.ixn.federation.model;

/*-
 * #%L
 * neighbour-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "capabilities")
public class Capabilities {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_generator")
	@SequenceGenerator(name="cap_generator", sequenceName = "cap_seq")
	@Column(name="cap_id")
	private Integer cap_id;

	private LocalDateTime lastCapabilityExchange;

	public LocalDateTime getLastCapabilityExchange() {
		return lastCapabilityExchange;
	}

	public void setLastCapabilityExchange(LocalDateTime lastCapabilityExchange) {
		this.lastCapabilityExchange = lastCapabilityExchange;
	}

	public enum CapabilitiesStatus{UNKNOWN, KNOWN, FAILED, UNREACHABLE}

	@Enumerated(EnumType.STRING)
	private CapabilitiesStatus status = CapabilitiesStatus.UNKNOWN;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id_dat", foreignKey = @ForeignKey(name="fk_dat_cap"))
	private Set<DataType> dataTypes = new HashSet<>();

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
		this.dataTypes.clear();
		if ( capabilities != null ) {
			this.dataTypes.addAll(capabilities);
		}
	}

	public boolean hasDataTypes() {
		return dataTypes.size() > 0;
	}

	@Override
	public String toString() {
		return "Capabilities{" +
				"cap_id=" + cap_id +
				", status=" + status +
				", dataTypes=" + dataTypes +
				", lastCapabilityExchange=" + lastCapabilityExchange +
				'}';
	}
}
