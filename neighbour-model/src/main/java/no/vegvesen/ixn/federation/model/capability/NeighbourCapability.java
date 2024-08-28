package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name="neighbour_capability")
public class NeighbourCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neigh_cap_seq")
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "app", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_neigh_cap_app"))
    private Application application;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "meta", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_neigh_cap_meta"))
    private Metadata metadata;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    public NeighbourCapability() {
    }

    public NeighbourCapability(Application application, Metadata metadata) {
        this.application = application;
        this.metadata = metadata;
    }

    public NeighbourCapability(Integer id, Application application, Metadata metadata) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourCapability that = (NeighbourCapability) o;
        return Objects.equals(application, that.application);
    }
    @Override
    public int hashCode(){
        return Objects.hash(application);
    }

    @Override
    public String toString() {
        return "NeighbourCapability{" +
                "id=" + id +
                ", application=" + application +
                ", metadata=" + metadata +
                '}';
    }
}
