package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "private_channels")
public class PrivateChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privchannel_seq")
    @Column(name="id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    private PrivateChannelStatus status;

    @Column
    private String peerName;

    @Column
    private String queueName;

    @Column
    private String serviceProviderName;
    public PrivateChannel() {

    }
/*
    public PrivateChannel(String peerName, PrivateChannelStatus status) {
        this.peerName = peerName;
        this.status = status;
    }

 */
    // NY
    public PrivateChannel(String peerName, PrivateChannelStatus status, String serviceProviderName) {
        this.peerName = peerName;
        this.status = status;
        this.serviceProviderName = serviceProviderName;
    }


    public PrivateChannel(String peerName, String queueName, PrivateChannelStatus status, String serviceProviderName) {
        this.peerName = peerName;
        this.queueName = queueName;
        this.status = status;
        this.serviceProviderName = serviceProviderName;
    }

    /*
    public PrivateChannel(String peerName, String queueName, PrivateChannelStatus status) {
        this.peerName = peerName;
        this.queueName = queueName;
        this.status = status;
    }
*/
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String uuid) {
        this.queueName = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrivateChannel)) return false;
        PrivateChannel that = (PrivateChannel) o;
        return peerName.equals(that.peerName) &&
                Objects.equals(queueName, that.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerName, queueName);
    }

    @Override
    public String toString() {
        return "PrivateChannel{" +
                "id=" + id +
                ", status=" + status +
                ", peerName='" + peerName + '\'' +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
