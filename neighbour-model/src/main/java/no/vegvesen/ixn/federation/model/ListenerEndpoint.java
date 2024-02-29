package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "listener_endpoints", uniqueConstraints = @UniqueConstraint(columnNames = {"neighbourName", "source", "target"}, name = "uk_listener_endpoint"))
public class ListenerEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "listener_end_seq")
    @Column(name = "id")
    private Integer id;

    private String neighbourName;
    private String source;
    private String host;
    private Integer port;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "mes_con", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_listener_endpoints_message_connection"))
    private Connection messageConnection;

    private int maxBandwidth;
    private int maxMessageRate;

    private String target;


    public ListenerEndpoint() {

    }

    public ListenerEndpoint(String neighbourName, String source, String host, Integer port, Connection messageConnection, String target) {
        this.neighbourName = neighbourName;
        this.source = source;
        this.host = host;
        this.port = port;
        this.messageConnection = messageConnection;
        this.target = target;
    }

    public ListenerEndpoint(String neighbourName, String source, String host, Integer port, Connection messageConnection, String target, int maxBandwidth, int maxMessageRate) {
        this.neighbourName = neighbourName;
        this.source = source;
        this.host = host;
        this.port = port;
        this.messageConnection = messageConnection;
        this.target = target;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public String getNeighbourName() { return neighbourName; }

    public String getSource() { return source; }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Connection getMessageConnection () { return messageConnection; }

    public int getMaxBandwidth() {
        return maxBandwidth;
    }

    public int getMaxMessageRate() {
        return maxMessageRate;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "ListenerEndpoint{" +
                "id=" + id +
                ", neighbourName='" + neighbourName + '\'' +
                ", source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", messageConnection=" + messageConnection +
                ", target=" + target +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListenerEndpoint)) return false;
        ListenerEndpoint that = (ListenerEndpoint) o;
        return neighbourName.equals(that.neighbourName) &&
                source.equals(that.source) &&
                target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbourName, source, target);
    }
}
