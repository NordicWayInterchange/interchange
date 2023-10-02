package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.RedirectStatusApi;
import no.vegvesen.ixn.federation.model.RedirectStatus;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "metadata")
public class Metadata {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meta_seq")
    private Integer id;

    private String infoUrl;

    private Integer shardCount;

    private RedirectStatus redirectPolicy;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private Integer repetitionInterval;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "met_id", foreignKey = @ForeignKey(name="fk_met"))
    private List<Shard> shards = new ArrayList<>();

    public Metadata() {

    }

    public Metadata(String infoUrl, Integer shardCount, RedirectStatus redirectPolicy, Integer maxBandwidth, Integer maxMessageRate, Integer repetitionInterval) {
        this.infoUrl = infoUrl;
        this.shardCount = shardCount;
        this.redirectPolicy = redirectPolicy;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.repetitionInterval = repetitionInterval;
    }

    //for testing
    public Metadata(RedirectStatus redirectPolicy) {
        this("", 1, redirectPolicy, 0, 0, 0);
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

    public Integer getShardCount() {
        return shardCount;
    }

    public void setShardCount(Integer shardCount) {
        this.shardCount = shardCount;
    }

    public RedirectStatus getRedirectPolicy() {
        return redirectPolicy;
    }

    public void setRedirectPolicy(RedirectStatus redirectPolicy) {
        this.redirectPolicy = redirectPolicy;
    }

    public Integer getMaxBandwidth() {
        return maxBandwidth;
    }

    public void setMaxBandwidth(Integer maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
    }

    public Integer getMaxMessageRate() {
        return maxMessageRate;
    }

    public void setMaxMessageRate(Integer maxMessageRate) {
        this.maxMessageRate = maxMessageRate;
    }

    public Integer getRepetitionInterval() {
        return repetitionInterval;
    }

    public void setRepetitionInterval(Integer repetitionInterval) {
        this.repetitionInterval = repetitionInterval;
    }

    public MetadataApi toApi() {
        return new MetadataApi(getShardCount(), getInfoUrl(), toRedirectStatusApi(getRedirectPolicy()), getMaxBandwidth(), getMaxMessageRate(), getRepetitionInterval());
    }

    public RedirectStatusApi toRedirectStatusApi(RedirectStatus status) {
        switch (status) {
            case MANDATORY:
                return RedirectStatusApi.MANDATORY;
            case NOT_AVAILABLE:
                return RedirectStatusApi.NOT_AVAILABLE;
            default:
                return RedirectStatusApi.OPTIONAL;
        }
    }

    public List<Shard> getShards() {
        return shards;
    }

    public void setShards(List<Shard> shards) {
        this.shards.clear();
        if (shards != null) {
            this.shards.addAll(shards);
        }
    }

    public boolean hasShards() {
        return !shards.isEmpty();
    }

    public void removeShards() {
        this.shards.clear();
    }

    public Set<String> getExchangesFromShards() {
        Set<String> exchanges = new HashSet<>();
        for (Shard shard : shards) {
            exchanges.add(shard.getExchangeName());
        }
        return exchanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(infoUrl, metadata.infoUrl) && Objects.equals(shardCount, metadata.shardCount) && redirectPolicy == metadata.redirectPolicy && Objects.equals(maxBandwidth, metadata.maxBandwidth) && Objects.equals(maxMessageRate, metadata.maxMessageRate) && Objects.equals(repetitionInterval, metadata.repetitionInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(infoUrl, shardCount, redirectPolicy, maxBandwidth, maxMessageRate, repetitionInterval);
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "infoUrl='" + infoUrl + '\'' +
                ", shardCount=" + shardCount +
                ", redirectPolicy=" + redirectPolicy +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", repetitionInterval=" + repetitionInterval +
                '}';
    }
}
