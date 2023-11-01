package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "subscription_shards")
public class SubscriptionShard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subshard_seq")
    @Column(name = "id")
    private Integer id;

    private String exchangeName;

    public SubscriptionShard() {

    }

    public SubscriptionShard(String exchangeName) {
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
        if (!(o instanceof SubscriptionShard)) return false;
        SubscriptionShard that = (SubscriptionShard) o;
        return exchangeName.equals(that.exchangeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeName);
    }

    @Override
    public String toString() {
        return "SubscriptionShard{" +
                "id=" + id +
                ", exchangeName='" + exchangeName + '\'' +
                '}';
    }
}
