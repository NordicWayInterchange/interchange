package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "listener_endpoints", uniqueConstraints = @UniqueConstraint(columnNames = {"neighbourName", "brokerUrl", "queue"}, name = "uk_listener_endpoint"))
public class ListenerEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "listener_end_seq")
    @Column(name = "id")
    private Integer id;

    private String neighbourName;
    private String brokerUrl;
    private String queue;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "mes_con", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_listener_endpoints_message_connection"))
    private Connection messageConnection;

    private int maxBandwidth;
    private int maxMessageRate;


    public ListenerEndpoint() { }

    public ListenerEndpoint(String neighbourName, String brokerUrl, String queue, Connection messageConnection) {
        this.neighbourName = neighbourName;
        this.brokerUrl = brokerUrl;
        this.queue = queue;
        this.messageConnection = messageConnection;
    }

    public ListenerEndpoint(String neighbourName, String brokerUrl, String queue, Connection messageConnection, int maxBandwidth, int maxMessageRate) {
        this.neighbourName = neighbourName;
        this.brokerUrl = brokerUrl;
        this.queue = queue;
        this.messageConnection = messageConnection;
        this.maxBandwidth = maxBandwidth;
        this.maxMessageRate = maxMessageRate;
    }

    public String getNeighbourName() { return neighbourName; }

    public void setNeighbourName(String neighbourName) { this.neighbourName = neighbourName; }

    public String getBrokerUrl() { return brokerUrl; }

    public void setBrokerUrl(String brokerUrl){ this.brokerUrl = brokerUrl; }

    public String getQueue() { return queue; }

    public void setQueue(String queue) { this.queue = queue; }

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

    @Override
    public String toString() {
        return "ListenerEndpoint{" +
                "id=" + id +
                ", neighbourName='" + neighbourName + '\'' +
                ", brokerUrl='" + brokerUrl + '\'' +
                ", queue='" + queue + '\'' +
                ", messageConnection=" + messageConnection +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListenerEndpoint that = (ListenerEndpoint) o;
        return neighbourName.equals(that.neighbourName) &&
                brokerUrl.equals(that.brokerUrl) &&
                queue.equals(that.queue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbourName, brokerUrl, queue);
    }

}
