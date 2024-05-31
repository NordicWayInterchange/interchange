package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import no.vegvesen.ixn.federation.model.capability.Capability;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof IncomingMatch)) return false;
        IncomingMatch that = (IncomingMatch) o;
        return Objects.equals(neighbourSubscription, that.neighbourSubscription) && Objects.equals(capability, that.capability);
    }
    @Override
    public int hashCode(){
        return Objects.hash(neighbourSubscription, capability);
    }
    @Override
    public String toString() {
        return "Match{" +
                "neighbourSubscription=" + neighbourSubscription +
                ", capability=" + capability +
                '}';
    }
}
