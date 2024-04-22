package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "local_subscription_shards")
public class LocalSubscriptionShard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locsubshard_seq")
    @Column(name = "id")
    private Integer id;

    String exchangeName;

    public LocalSubscriptionShard() {

    }

    public LocalSubscriptionShard(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscriptionShard that = (LocalSubscriptionShard) o;
        return Objects.equals(exchangeName, that.exchangeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeName);
    }

    @Override
    public String toString() {
        return "LocalSubscriptionShard{" +
                "id=" + id +
                ", exchangeName='" + exchangeName + '\'' +
                '}';
    }
}
