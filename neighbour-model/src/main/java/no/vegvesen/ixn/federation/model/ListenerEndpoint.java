package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

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
    private String messageChannelUrl;
    private String messageChannelPort;
    private static final String DEFAULT_MESSAGE_CHANNEL_PORT = "5671";
    private static Logger logger = LoggerFactory.getLogger(ListenerEndpoint.class);

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "mes_con", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_listener_endpoints_message_connection"))
    private Connection messageConnection;


    public ListenerEndpoint() { }

    public ListenerEndpoint(String neighbourName, String brokerUrl, String queue, Connection messageConnection, String messageChannelUrl, String messageChannelPort) {
        this.neighbourName = neighbourName;
        this.brokerUrl = brokerUrl;
        this.queue = queue;
        this.messageConnection = messageConnection;
        this.messageChannelUrl = messageChannelUrl;
        this.messageChannelPort = messageChannelPort;
    }

    public String getNeighbourName() { return neighbourName; }

    public void setNeighbourName(String neighbourName) { this.neighbourName = neighbourName; }

    public String getBrokerUrl() { return brokerUrl; }

    public void setBrokerUrl(String brokerUrl){ this.brokerUrl = brokerUrl; }

    public String getQueue() { return queue; }

    public void setQueue(String queue) { this.queue = queue; }

    public Connection getMessageConnection () { return messageConnection; }

    public void setMessageConnection (Connection messgeConnection) { this.messageConnection = messageConnection; }

    public String getMessageChannelUrl() {
        try {
            if (this.getMessageChannelPort() == null || this.getMessageChannelPort().equals(DEFAULT_MESSAGE_CHANNEL_PORT)) {
                return String.format("amqps://%s/", neighbourName);
            } else {
                return String.format("amqps://%s:%s/", neighbourName, this.getMessageChannelPort());
            }
        } catch (NumberFormatException e) {
            logger.error("Could not create message channel url for interchange {}", this, e);
            throw new DiscoveryException(e);
        }
    }

    public void setMessageChannelUrl (String messageChannelUrl) { this.messageChannelUrl = messageChannelUrl; }

    public String getMessageChannelPort () { return messageChannelPort;}

    public void setMessageChannelPort (String messageChannelPort) { this.messageChannelPort = messageChannelPort; }

    @Override
    public String toString() {
        return "ListenerEndpoint{" +
                "id=" + id +
                ", neighbourName='" + neighbourName + '\'' +
                ", brokerUrl='" + brokerUrl + '\'' +
                ", queue='" + queue + '\'' +
                ", messageConnection=" + messageConnection +
                '}';
    }
}
