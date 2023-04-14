package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CapabilityMetadataApi {

    private Integer shardCount = 1;

    private String infoUrl;

    private RedirectStatusApi redirectPolicy;

    private Integer maxBandwidth;

    private Integer maxMessageRate;

    private Integer repetitionInterval;

    public CapabilityMetadataApi() {

    }

    public CapabilityMetadataApi(Integer shardCount, String infoUrl, RedirectStatusApi redirectPolicy, Integer maxBandwidth, Integer maxMessageRate, Integer repetitionInterval) {
        this.shardCount = shardCount;
        this.infoUrl = infoUrl;
        this.redirectPolicy = redirectPolicy;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
        this.repetitionInterval = repetitionInterval;
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

    public RedirectStatusApi getRedirectPolicy() {
        return redirectPolicy;
    }

    public void setRedirectPolicy(RedirectStatusApi redirectPolicy) {
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

    @Override
    public String toString() {
        return "CapabilityMetadataApi{" +
                "shardCount=" + shardCount +
                ", infoUrl='" + infoUrl + '\'' +
                ", redirectPolicy=" + redirectPolicy +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                ", repetitionInterval=" + repetitionInterval +
                '}';
    }
}
