package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="private_channel_endpoints")
public class PrivateChannelEndpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "local_endpoint_seq")
    @Column
    private Integer id;
    @Column
    private String source;
    @Column
    private Integer port;
    @Column
    private String queueName;

    public PrivateChannelEndpoint(String host, Integer port, String target) {
        this.source = host;
        this.port = port;
        this.queueName = target;
    }

    public PrivateChannelEndpoint() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String host) {
        this.source = host;
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
        return source.equals(that.source) &&
                source.equals(that.source) &&
                port.equals(that.port);
    }
    @Override
    public int hashCode() {
        return Objects.hash(source, queueName, port);
    }

    @Override
    public String toString() {
        return "LocalEndpoint{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", port=" + port +
                ", target=" + queueName +
                '}';
    }
}
