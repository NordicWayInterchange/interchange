package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "local_endpoints")
public class LocalEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_endpoint_seq")
    @Column(name = "id")
    private Integer id;

    private String source;
    private String messageBrokerUrl;
    private String host;
    private Integer port;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public LocalEndpoint() {

    }

    public LocalEndpoint(String source, String messageBrokerUrl) {
        this.source = source;
        this.messageBrokerUrl = messageBrokerUrl;
    }

    public LocalEndpoint(String source, String host, Integer port) {
        this.source = source;
        this.host = host;
        this.port = port;
    }

    public LocalEndpoint(String source, String host, Integer port, Integer maxBandwidth, Integer maxMessageRate) {
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

    public String getMessageBrokerUrl() {
        return messageBrokerUrl;
    }

    public void setMessageBrokerUrl(String messageBrokerUrl) {
        this.messageBrokerUrl = messageBrokerUrl;
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
        if(source.equals(listenerEndpoint.getSource()) && messageBrokerUrl.equals(listenerEndpoint.getBrokerUrl())){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalEndpoint)) return false;
        LocalEndpoint that = (LocalEndpoint) o;
        return id.equals(that.id) &&
                source.equals(that.source) &&
                host.equals(that.host) &&
                port.equals(that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, host, port);
    }

    @Override
    public String toString() {
        return "LocalEndpoint{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
