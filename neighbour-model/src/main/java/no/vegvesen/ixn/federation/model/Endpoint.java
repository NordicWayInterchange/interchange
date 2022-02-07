package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "endpoints")
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endpoint_seq")
    @Column(name = "id")
    private Integer id;

    private String source;
    private String host;
    private Integer port;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public Endpoint() {

    }

    public Endpoint(String source, String host, Integer port) {
        this.source = source;
        this.host = host;
        this.port = port;
    }

    public Endpoint(String source, String host, Integer port, Integer maxBandwidth, Integer maxMessageRate) {
        this.source = source;
        this.host = host;
        this.port = port;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public boolean isTheSameAsListenerEndpoint(ListenerEndpoint listenerEndpoint) {
        return source.equals(listenerEndpoint.getSource()) && host.equals(listenerEndpoint.getHost()) && port.equals(listenerEndpoint.getPort());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endpoint)) return false;
        Endpoint endpoint = (Endpoint) o;
        return id.equals(endpoint.id) &&
                source.equals(endpoint.source) &&
                host.equals(endpoint.host) &&
                port.equals(endpoint.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, host, port);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
