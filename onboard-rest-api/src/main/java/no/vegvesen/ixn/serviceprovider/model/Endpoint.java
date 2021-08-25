package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class Endpoint {
    private String url;
    private String source;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public Endpoint() {
    }

    public Endpoint(String url, String source) {
        this.url = url;
        this.source = source;
    }

    public Endpoint(String url, String source, Integer maxBandwidth, Integer maxMessageRate) {
        this.url = url;
        this.source = source;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(url, endpoint.url) && Objects.equals(source, endpoint.source) && Objects.equals(maxBandwidth, endpoint.maxBandwidth) && Objects.equals(maxMessageRate, endpoint.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, source, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "url='" + url + '\'' +
                ", source='" + source + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
