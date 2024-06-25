package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "capability")
public class Capability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_plit_seq")
    private Integer id;

    private String uuid = UUID.randomUUID().toString();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "app", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cap_app"))
    private Application application;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "meta", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cap_meta"))
    private Metadata metadata;

    @Enumerated(EnumType.STRING)
    private CapabilityStatus status = CapabilityStatus.CREATED;

    public Capability() {

    }

    public Capability(Application application, Metadata metadata) {
        this.application = application;
        this.metadata = metadata;
    }

    public Capability(String uuid, Application application, Metadata metadata) {
        this.application = application;
        this.metadata = metadata;
        this.uuid = uuid;
    }

    public Capability(Integer id, Application application, Metadata metadata) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public CapabilityStatus getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatus status) {
        this.status = status;
    }

    public boolean isSharded() {
        return metadata.getShardCount() > 1;
    }

    public boolean hasShards() {
        return metadata.hasShards();
    }

    public static Set<Capability> transformNeighbourCapabilityToCapability(Set<NeighbourCapability> neighbourCapabilities){
        Set<Capability> capabilities = new HashSet<>();
        for(NeighbourCapability i : neighbourCapabilities){
            capabilities.add(
                    new Capability(i.getApplication(), i.getMetadata())
            );

        }
        return capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capability that = (Capability) o;
        return Objects.equals(application, that.application);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application);
    }

    @Override
    public String toString() {
        return "Capability{" +
                "id=" + id +
                "uuid="+uuid +
                ", application=" + application +
                ", metadata=" + metadata +
                ", status=" + status + '\'' +
                '}';
    }
}
