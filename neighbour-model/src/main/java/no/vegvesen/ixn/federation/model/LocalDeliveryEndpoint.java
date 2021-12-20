package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_delivery_endpoints")
public class LocalDeliveryEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_del_endpoint_seq")
    @Column(name = "id")
    private Integer id;

    private String host;
    private int port;
    private String target;
    private String selector;
    private int maxBandwidth;
    private int maxMessageRate;

    public LocalDeliveryEndpoint() {
    }

    public LocalDeliveryEndpoint(String host, int port, String target, String selector, Integer maxBandwidth, Integer maxMessageRate) {
        this.host = host;
        this.port = port;
        this.target = target;
        this.selector = selector;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public LocalDeliveryEndpoint(String host, int port, String target, String selector) {
        this.host = host;
        this.port = port;
        this.target = target;
        this.selector = selector;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
        if (!(o instanceof LocalDeliveryEndpoint)) return false;
        LocalDeliveryEndpoint that = (LocalDeliveryEndpoint) o;
        return port == that.port &&
                maxBandwidth == that.maxBandwidth &&
                maxMessageRate == that.maxMessageRate &&
                Objects.equals(host, that.host) &&
                Objects.equals(target, that.target) &&
                Objects.equals(selector, that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, target, selector, maxBandwidth, maxMessageRate);
    }

    @Override
    public String toString() {
        return "LocalDeliveryEndpoint{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", target='" + target + '\'' +
                ", selector='" + selector + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
