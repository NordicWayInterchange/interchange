package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_delivery_endpoints", uniqueConstraints = @UniqueConstraint(columnNames = {"host", "port", "target"}))
public class LocalDeliveryEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_del_endpoint_seq")
    @Column(name = "id")
    private Integer id;

    private String host;
    private int port;
    private String target;

    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public LocalDeliveryEndpoint() {
    }

    public LocalDeliveryEndpoint(Integer id, String host, int port, String target, Integer maxBandwidth, Integer maxMessageRate) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.target = target;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public LocalDeliveryEndpoint(String host, int port, String target, Integer maxBandwidth, Integer maxMessageRate) {
        this(null,host,port,target,maxBandwidth,maxMessageRate);
    }

    public LocalDeliveryEndpoint(String host, int port, String target) {
        this(null,host,port,target, null,null);
    }

    public LocalDeliveryEndpoint(Integer id, String host, int port, String target) {
        this(id,host,port,target,null,null);
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
                Objects.equals(host, that.host) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, target);
    }

    @Override
    public String toString() {
        return "LocalDeliveryEndpoint{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", target='" + target + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
