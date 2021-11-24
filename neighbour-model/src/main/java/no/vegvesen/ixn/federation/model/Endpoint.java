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
    private String messageBrokerUrl;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    public Endpoint() {

    }

    public Endpoint(String source, String messageBrokerUrl) {
        this.source = source;
        this.messageBrokerUrl = messageBrokerUrl;
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
        if (!(o instanceof Endpoint)) return false;
        Endpoint endpoint = (Endpoint) o;
        return id.equals(endpoint.id) &&
                source.equals(endpoint.source) &&
                messageBrokerUrl.equals(endpoint.messageBrokerUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, messageBrokerUrl);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", messageBrokerUrl='" + messageBrokerUrl + '\'' +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
