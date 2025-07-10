package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.RedirectStatusApi;
import no.vegvesen.ixn.federation.model.RedirectStatus;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "metadata")
public class Metadata {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meta_seq")
    private Integer id;

    private String infoUrl;

    private Integer shardCount = 1;

    @Enumerated(EnumType.STRING)
    private RedirectStatus redirectPolicy = RedirectStatus.OPTIONAL;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private Integer repetitionInterval;

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
        this.redirectPolicy = redirectPolicy;
    }

    //for testing
    public Metadata(Integer shardCount) {
        this.shardCount = shardCount;
    }

    //for testing
    public Metadata(RedirectStatus redirectStatus, Integer shardCount) {
        this.redirectPolicy = redirectStatus;
        this.shardCount = shardCount;
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
        return new MetadataApi(
                getShardCount(),
                getInfoUrl(),
                redirectStatusToRedirectStatusApi(getRedirectPolicy()),
                getMaxBandwidth(),
                getMaxMessageRate(),
                getRepetitionInterval()
        );
    }

    public RedirectStatusApi redirectStatusToRedirectStatusApi(RedirectStatus redirectStatus) {
        if (redirectStatus == null) {
            return RedirectStatusApi.OPTIONAL;
        }
        switch (redirectStatus) {
            case MANDATORY -> {
                return RedirectStatusApi.MANDATORY;
            }
            case NOT_AVAILABLE ->  {
                return RedirectStatusApi.NOT_AVAILABLE;
            }
            default -> {
                return RedirectStatusApi.OPTIONAL;
            }
        }
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
