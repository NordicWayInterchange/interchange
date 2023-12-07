package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name="private_channel_endpoints")
public class PrivateChannelEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privatechannel_endpoint_seq")
    @Column
    private Integer id;

    @Column
    private String host;

    @Column
    private Integer port;

    @Column
    private String queueName;

    public PrivateChannelEndpoint(String host, Integer port, String queueName) {
        this.host = host;
        this.port = port;
        this.queueName = queueName;
    }

    public PrivateChannelEndpoint() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String target) {
        this.queueName = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocalEndpoint)) return false;
        PrivateChannelEndpoint that = (PrivateChannelEndpoint) o;
        return host.equals(that.host) &&
                host.equals(that.host) &&
                port.equals(that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, queueName, port);
    }

    @Override
    public String toString() {
        return "PrivateChannelEndpoint{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", queueName=" + queueName +
                '}';
    }
}
