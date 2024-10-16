package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "private_channels")
public class PrivateChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privchannel_seq")
    @Column(name="id")
    private Integer id;

    @Column
    private String uuid = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    private PrivateChannelStatus status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name="peer_id", foreignKey = @ForeignKey(name="fk_peer_privatechannel"))
    private Set<Peer> peers;

    @Column
    private String serviceProviderName;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name="end_id", foreignKey = @ForeignKey(name="fk_end_privatechannel"))
    private PrivateChannelEndpoint endpoint;

    public PrivateChannel() {

    }

    public PrivateChannel(Set<Peer> peers, PrivateChannelStatus status, String serviceProviderName) {
        this.peers = peers;
        this.status = status;
        this.serviceProviderName = serviceProviderName;
    }

    public PrivateChannel(Set<Peer> peers, PrivateChannelStatus status, PrivateChannelEndpoint privateChannelEndpoint, String serviceProviderName) {
        this.peers = peers;
        this.status = status;
        this.endpoint = privateChannelEndpoint;
        this.serviceProviderName = serviceProviderName;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<Peer> getPeers() {
        return peers;
    }

    public void setPeers(Set<Peer> peers) {
        this.peers = peers;
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
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannel that = (PrivateChannel) o;
        return Objects.equals(uuid, that.uuid) && status == that.status && Objects.equals(peers, that.peers) && Objects.equals(serviceProviderName, that.serviceProviderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, status, peers, serviceProviderName);
    }

    @Override
    public String toString() {
        return "PrivateChannel{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", status=" + status +
                ", peers=" + peers +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", endpoint=" + endpoint +
                '}';
    }
}
