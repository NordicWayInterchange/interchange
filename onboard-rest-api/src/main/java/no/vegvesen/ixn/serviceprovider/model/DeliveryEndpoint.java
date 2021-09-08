package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class DeliveryEndpoint {
    private String url;
    private String target;
    private String selector;
    private int maxBandwidth;
    private int maxMessageRate;

    public DeliveryEndpoint() {
    }

    public DeliveryEndpoint(String url, String target, String selector, Integer maxBandwidth, Integer maxMessageRate) {
        this.url = url;
        this.target = target;
        this.selector = selector;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public DeliveryEndpoint(String url, String target, String selector) {
        this.url = url;
        this.target = target;
        this.selector = selector;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
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
        DeliveryEndpoint that = (DeliveryEndpoint) o;
        return Objects.equals(url, that.url) && Objects.equals(target, that.target) && Objects.equals(selector, that.selector) && Objects.equals(maxBandwidth, that.maxBandwidth) && Objects.equals(maxMessageRate, that.maxMessageRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, target, selector, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "DeliveryEndpoint{" +
                "url='" + url + '\'' +
                ", target='" + target + '\'' +
                ", selector='" + selector + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }

}
