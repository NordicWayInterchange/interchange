package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.model.RedirectStatus;

import jakarta.persistence.*;
import no.vegvesen.ixn.shared.capability.MetadataApi;
import no.vegvesen.ixn.shared.capability.RedirectStatusApi;

import java.util.*;

@Entity
@Table(name = "metadata")
public class Metadata {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meta_seq")
    private Integer id;

    private String infoUrl;

    private RedirectStatus redirectPolicy;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private Integer repetitionInterval;

    public Metadata() {

    }

    public Metadata(String infoUrl, RedirectStatus redirectPolicy, Integer maxBandwidth, Integer maxMessageRate, Integer repetitionInterval) {
        this.infoUrl = infoUrl;
        this.redirectPolicy = redirectPolicy;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.repetitionInterval = repetitionInterval;
    }

    //for testing
    public Metadata(RedirectStatus redirectPolicy) {
        this("", redirectPolicy, 0, 0, 0);
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
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

    public MetadataApi toApi(int shardCount) {
        return new MetadataApi(shardCount, getInfoUrl(), toRedirectStatusApi(getRedirectPolicy()), getMaxBandwidth(), getMaxMessageRate(), getRepetitionInterval());
    }

    public RedirectStatusApi toRedirectStatusApi(RedirectStatus status) {
        if (status == null) {
            return RedirectStatusApi.OPTIONAL;
        }
        switch (status) {
            case MANDATORY:
                return RedirectStatusApi.MANDATORY;
            case NOT_AVAILABLE:
                return RedirectStatusApi.NOT_AVAILABLE;
            default:
                return RedirectStatusApi.OPTIONAL;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata metadata = (Metadata) o;
        return Objects.equals(infoUrl, metadata.infoUrl) && redirectPolicy == metadata.redirectPolicy && Objects.equals(maxBandwidth, metadata.maxBandwidth) && Objects.equals(maxMessageRate, metadata.maxMessageRate) && Objects.equals(repetitionInterval, metadata.repetitionInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(infoUrl, redirectPolicy, maxBandwidth, maxMessageRate, repetitionInterval);
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "infoUrl='" + infoUrl + '\'' +
                ", redirectPolicy=" + redirectPolicy +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", repetitionInterval=" + repetitionInterval +
                '}';
    }
}
