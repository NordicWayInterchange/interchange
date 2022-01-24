package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "listener_endpoints", uniqueConstraints = @UniqueConstraint(columnNames = {"neighbourName", "source", "host", "port"}, name = "uk_listener_endpoint"))
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

    private String exchangeName;


    public ListenerEndpoint() { }

    public ListenerEndpoint(String neighbourName, String source, String host, Integer port, Connection messageConnection) {
        this.neighbourName = neighbourName;
        this.source = source;
        this.host = host;
        this.port = port;
        this.messageConnection = messageConnection;
    }

    public ListenerEndpoint(String neighbourName, String source, String host, Integer port, Connection messageConnection, String exchangeName) {
        this.neighbourName = neighbourName;
        this.source = source;
        this.host = host;
        this.port = port;
        this.messageConnection = messageConnection;
        this.exchangeName = exchangeName;
    }

    public ListenerEndpoint(String neighbourName, String source, String host, Integer port, Connection messageConnection, int maxBandwidth, int maxMessageRate) {
        this.neighbourName = neighbourName;
        this.source = source;
        this.host = host;
        this.port = port;
        this.messageConnection = messageConnection;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public String getNeighbourName() { return neighbourName; }

    public void setNeighbourName(String neighbourName) { this.neighbourName = neighbourName; }

    public String getSource() { return source; }

    public void setSource(String source) { this.source = source; }

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

    public Connection getMessageConnection () { return messageConnection; }

    public void setMessageConnection (Connection messageConnection) { this.messageConnection = messageConnection; }

    public int getMaxBandwidth() {
        return maxBandwidth;
    }

    public void setMaxBandwidth(int maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
    }

    public int getMaxMessageRate() {
        return maxMessageRate;
    }

    public void setMaxMessageRate(int maxMessageRate) {
        this.maxMessageRate = maxMessageRate;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
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
                host.equals(that.host) &&
                port.equals(that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbourName, source, host, port);
    }
}
