package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;
import java.util.Set;

public class MetadataImportApi {

    private Integer shardCount;

    private String infoUrl;

    private RedirectStatusImportApi redirectPolicy;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private Integer repetitionInterval;

    private Set<CapabilityShardImportApi> shards;

    public enum RedirectStatusImportApi {
        OPTIONAL, MANDATORY, NOT_AVAILABLE
    }

    public MetadataImportApi() {

    }

    public MetadataImportApi(Integer shardCount,
                             String infoUrl,
                             RedirectStatusImportApi redirectPolicy,
                             Integer maxBandwidth,
                             Integer maxMessageRate,
                             Integer repetitionInterval,
                             Set<CapabilityShardImportApi> shards) {
        this.shardCount = shardCount;
        this.infoUrl = infoUrl;
        this.redirectPolicy = redirectPolicy;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.repetitionInterval = repetitionInterval;
        this.shards = shards;
    }

    public Integer getShardCount() {
        return shardCount;
    }

    public void setShardCount(Integer shardCount) {
        this.shardCount = shardCount;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

    public RedirectStatusImportApi getRedirectPolicy() {
        return redirectPolicy;
    }

    public void setRedirectPolicy(RedirectStatusImportApi redirectPolicy) {
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

    public Set<CapabilityShardImportApi> getShards() {
        return shards;
    }

    public void setShards(Set<CapabilityShardImportApi> shards) {
        this.shards = shards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataImportApi that = (MetadataImportApi) o;
        return Objects.equals(shardCount, that.shardCount) && Objects.equals(infoUrl, that.infoUrl) && redirectPolicy == that.redirectPolicy && Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate) && Objects.equals(repetitionInterval, that.repetitionInterval) && Objects.equals(shards, that.shards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardCount, infoUrl, redirectPolicy, maxBandwidth, maxMessageRate, repetitionInterval, shards);
    }

    @Override
    public String toString() {
        return "MetadataImportApi{" +
                "shardCount=" + shardCount +
                ", infoUrl='" + infoUrl + '\'' +
                ", redirectPolicy=" + redirectPolicy +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", repetitionInterval=" + repetitionInterval +
                ", shards=" + shards +
                '}';
    }
}
