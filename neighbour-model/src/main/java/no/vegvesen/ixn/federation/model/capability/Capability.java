package no.vegvesen.ixn.federation.model.capability;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import no.vegvesen.ixn.federation.capability.MessageValidatingSelectorCreator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.*;

import java.util.UUID;

/**
 * This class takes both a list of CapabilityShards, and a ShardCound in the metadata section.
 * We are working towards only having the list, not the count (so we need to check that we have shardCound number of
 * shards in the list).
 */
@Entity
@Table(name = "capability")
@JsonIgnoreProperties(value = "createdTimestamp")
public class Capability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_plit_seq")
    private Integer id;

    @Column(nullable = false)
    private String uuid = UUID.randomUUID().toString();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "app", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cap_app"))
    private Application application;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "meta", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cap_meta"))
    private Metadata metadata;

    @Enumerated(EnumType.STRING)
    private CapabilityStatus status = CapabilityStatus.REQUESTED;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "cap_shard_id", foreignKey = @ForeignKey(name="fk_cap_shard"))
    private List<CapabilityShard> shards = new ArrayList<>();

    private LocalDateTime createdTimestamp;

    public Capability() {
        this.createdTimestamp = LocalDateTime.now();
    }

    public Capability(Application application) {
        this.application = application;
        this.metadata = new Metadata();
        this.createdTimestamp = LocalDateTime.now();
    }
    public Capability(Application application, Metadata metadata) {
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = LocalDateTime.now();
        this.shards.addAll(createCapabilityShards(application, metadata.getShardCount()));
    }


    public Capability(Application application, Metadata metadata, List<CapabilityShard> shards) {
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = LocalDateTime.now();
        this.shards.addAll(shards);
        if (metadata.getShardCount() != shards.size()) {
            throw new IllegalArgumentException("metadata shardcount must match the capability shard list length");
        }
        this.createdTimestamp =  LocalDateTime.now();
    }

    public Capability(String uuid, Application application, Metadata metadata) {
        this.application = application;
        this.metadata = metadata;
        this.uuid = uuid;
        this.createdTimestamp = LocalDateTime.now();
        this.shards.addAll(createCapabilityShards(application, metadata.getShardCount()));
    }

    public Capability(Integer id, Application application, Metadata metadata) {
        this.id = id;
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = LocalDateTime.now();
        this.shards.addAll(createCapabilityShards(application, metadata.getShardCount()));
    }

    public Capability(Application application, Metadata metadata, LocalDateTime createdTimestamp) {
        this.application = application;
        this.metadata = metadata;
        this.createdTimestamp = createdTimestamp;
        this.shards.addAll(createCapabilityShards(application, metadata.getShardCount()));
    }

    public Capability(String uuid, Application application, Metadata metadata, List<CapabilityShard> shards) {
        this.application = application;
        this.metadata = metadata;
        this.uuid = uuid;
        this.createdTimestamp = LocalDateTime.now();
        this.shards.addAll(shards);
        if (metadata.getShardCount() != shards.size()) {
            throw new IllegalArgumentException("metadata shardcount must match the capability shard list length");
        }
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
        return shards.size() > 1;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime lastUpdatedTimestamp) {
        this.createdTimestamp = lastUpdatedTimestamp;
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

    public Optional<CapabilityShard> getShard(Integer id) {
        return shards.stream()
                .filter(shard -> shard.getShardId().equals(id))
                .findFirst();
    }

    public void addShards(List<CapabilityShard> shards){
        this.shards.addAll(shards);
    }

    public boolean hasShards() {
        return !shards.isEmpty();
    }

    public void removeShards() {
        this.shards.clear();
    }

    public Set<String> getExchangesFromShards() {
        Set<String> exchanges = new HashSet<>();
        for (CapabilityShard shard : shards) {
            exchanges.add(shard.getExchangeName());
        }
        return exchanges;
    }

    public int shardCount() {
        return shards.size();
    }


    public static List<CapabilityShard> createCapabilityShards(Application application, int shardCount) {
        List<CapabilityShard> capabilityShards = new ArrayList<>();
        for (int i = 0; i < shardCount; i++) {
            String exchangeName = "cap-" + UUID.randomUUID();
            String capabilitySelector;
            if (shardCount > 1) {
                capabilitySelector = MessageValidatingSelectorCreator.makeSelector(application, i+1);
            } else {
                capabilitySelector = MessageValidatingSelectorCreator.makeSelector(application,null);
            }
            CapabilityShard shard = new CapabilityShard(i + 1,exchangeName, capabilitySelector);
            capabilityShards.add(shard);
        }
        return capabilityShards;
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
                ", status=" + status +
                ", shards=" + shards +
                '}';
    }
}
