package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "delivery_shards")
public class DeliveryShard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delshard_seq")
    @Column(name = "id")
    private Integer id;

    private String exchangeName;

    public DeliveryShard() {

    }

    public DeliveryShard(String exchangeName) {
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
        DeliveryShard that = (DeliveryShard) o;
        return Objects.equals(exchangeName, that.exchangeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeName);
    }

    @Override
    public String toString() {
        return "DeliveryShard{" +
                "id=" + id +
                ", exchangeName='" + exchangeName + '\'' +
                '}';
    }
}
