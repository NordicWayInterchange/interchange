package no.vegvesen.ixn.federation.model;


import javax.persistence.*;

@Entity
@Table(name = "listener_endpoints")
public class ListenerEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "listener_end_seq")
    @Column(name = "id")
    private Integer id;

    private String neighbourName;
    private String brokerUrl;
    private String queue;

    //@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    //@JoinColumn(name = "mes_con", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_listener_endpoints_message_connection"))
    //private Connection messageConnection;


    public ListenerEndpoint() { }

    public ListenerEndpoint(String neighbourName, String brokerUrl, String queue) {
        this.neighbourName = neighbourName;
        this.brokerUrl = brokerUrl;
        this.queue = queue;
        //this.messageConnection = messageConnection;
    }

    public String getNeighbourName() { return neighbourName; }

    public void setNeighbourName(String neighbourName) { this.neighbourName = neighbourName; }

    public String getBrokerUrl() { return brokerUrl; }

    public void setBrokerUrl(String brokerUrl){ this.brokerUrl = brokerUrl; }

    public String getQueue() { return queue; }

    public void setQueue(String queue) { this.queue = queue; }

    //public Connection getMessageConnection () { return messageConnection; }

    //public void setMessageConnection (Connection messgeConnection) { this.messageConnection = messageConnection; }
}
