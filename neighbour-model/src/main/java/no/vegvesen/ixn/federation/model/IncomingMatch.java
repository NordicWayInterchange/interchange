package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import no.vegvesen.ixn.federation.model.capability.Capability;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="incoming_matches")
public class IncomingMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inc_match_seq")
    private Integer id;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private NeighbourSubscription neighbourSubscription;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name="incoming_match_id")
    private Set<Capability> capabilities = new HashSet<>();

    public IncomingMatch(){}

    public IncomingMatch(NeighbourSubscription neighbourSubscription, Set<Capability> capabilities){
        this.neighbourSubscription = neighbourSubscription;
        addCapabilities(capabilities);
    }

    public void addCapability(Capability capability){
        this.capabilities.add(capability);
    }
    public void addCapabilities(Set<Capability> capabilities){
        this.capabilities.addAll(capabilities);
    }
    public NeighbourSubscription getNeighbourSubscription() {
        return neighbourSubscription;
    }

    public void setNeighbourSubscription(NeighbourSubscription neighbourSubscription) {
        this.neighbourSubscription = neighbourSubscription;
    }

    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<Capability> capabilities) {
        this.capabilities.clear();
        if(capabilities != null){
            this.capabilities.addAll(capabilities);
        }
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
