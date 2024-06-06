package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "private_channels")
public class PrivateChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privchannel_seq")
    @Column(name="id")
    private Integer id;

    @Column
    private UUID uuid = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    private PrivateChannelStatus status;

    @Column
    private String peerName;
    @Column
    private String serviceProviderName;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name="end_id", foreignKey = @ForeignKey(name="fk_end_privatechannel"))
    private PrivateChannelEndpoint endpoint;

    public PrivateChannel() {

    }

    public PrivateChannel(String peerName, PrivateChannelStatus status, String serviceProviderName) {
        this.peerName = peerName;
        this.status = status;
        this.serviceProviderName = serviceProviderName;
    }
    public PrivateChannel(String peerName, PrivateChannelStatus status, PrivateChannelEndpoint privateChannelEndpoint, String serviceProviderName) {
        this.peerName = peerName;
        this.status = status;
        this.endpoint = privateChannelEndpoint;
        this.serviceProviderName = serviceProviderName;
    }
    public PrivateChannel(UUID uuid, String peerName, PrivateChannelStatus status, PrivateChannelEndpoint privateChannelEndpoint, String serviceProviderName) {
        this.peerName = peerName;
        this.status = status;
        this.endpoint = privateChannelEndpoint;
        this.serviceProviderName = serviceProviderName;
        this.uuid = uuid;
    }

    public PrivateChannelEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(PrivateChannelEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public PrivateChannelStatus getStatus() {
        return status;
    }

    public void setStatus(PrivateChannelStatus status) {
        this.status = status;
    }


    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrivateChannel)) return false;
        PrivateChannel that = (PrivateChannel) o;
        return peerName.equals(that.peerName) &&
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerName, endpoint);
    }

    @Override
    public String toString() {
        return "PrivateChannel{" +
                "id=" + id +
                "uuid=" + uuid +
                ", status=" + status +
                ", peerName='" + peerName + '\'' +
                ", serviceProviderName='"+serviceProviderName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
