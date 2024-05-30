package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import no.vegvesen.ixn.federation.model.capability.Capability;

@Entity
@Table(name="incoming_matches")
public class IncomingMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inc_match_seq")
    private Integer id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private NeighbourSubscription neighbourSubscription;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name="incoming_match_id")
    private Capability capability;

    public IncomingMatch(){}

    public IncomingMatch(NeighbourSubscription neighbourSubscription, Capability capability){
        this.neighbourSubscription = neighbourSubscription;
        this.capability = capability;
    }

    public NeighbourSubscription getNeighbourSubscription() {
        return neighbourSubscription;
    }

    public void setNeighbourSubscription(NeighbourSubscription neighbourSubscription) {
        this.neighbourSubscription = neighbourSubscription;
    }

    public Capability getCapability() {
        return capability;
    }

    public void setCapability(Capability capability) {
        this.capability = capability;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
