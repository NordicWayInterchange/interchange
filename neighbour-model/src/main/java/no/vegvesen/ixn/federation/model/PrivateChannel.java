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

    @Column(columnDefinition="TEXT")
    private String clientName;

    @Column
    private String queueName;

    public PrivateChannel() {
        this.queueName = UUID.randomUUID().toString();
    }

    public PrivateChannel(String clientName, PrivateChannelStatus status) {
        this.clientName = clientName;
        this.status = status;
        this.queueName = UUID.randomUUID().toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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
        return clientName.equals(that.clientName) &&
                queueName.equals(that.queueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientName, queueName);
    }

    @Override
    public String toString() {
        return "PrivateChannel{" +
                "id=" + id +
                ", status=" + status +
                ", clientName='" + clientName + '\'' +
                ", queueName='" + queueName + '\'' +
                '}';
    }
}
