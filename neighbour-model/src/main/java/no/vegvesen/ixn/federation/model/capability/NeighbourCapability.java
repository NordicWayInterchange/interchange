package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "neigh_cap_shard_id", foreignKey = @ForeignKey(name = "fk_neigh_cap_shard"))
    private List<CapabilityShard> shards = new ArrayList<>();

    private LocalDateTime createdTimestamp;

    public NeighbourCapability() {
        this.createdTimestamp = LocalDateTime.now();
    }

    public NeighbourCapability(Application application, Metadata metadata) {
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = LocalDateTime.now();
    }

    public NeighbourCapability(Application application, Metadata metadata, List<CapabilityShard> shards) {
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = LocalDateTime.now();
        this.shards.addAll(shards);
    }

    public NeighbourCapability(Integer id, Application application, Metadata metadata) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = LocalDateTime.now();
    }

    public NeighbourCapability(Integer id, Application application, Metadata metadata, List<CapabilityShard> shards) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = LocalDateTime.now();
        this.shards.addAll(shards);
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

    public List<CapabilityShard> getShards() {
        return shards;
    }

    public void setShards(List<CapabilityShard> shards) {
        this.shards.clear();
        if (shards != null) {
            this.shards.addAll(shards);
        }
    }

    public int getShardCount() {
        return shards.size();
    }

    public boolean hasShards() {
        return !shards.isEmpty();
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public boolean isSharded() {
        return getShardCount() > 1;
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
                ", shards=" + shards +
                '}';
    }
}
