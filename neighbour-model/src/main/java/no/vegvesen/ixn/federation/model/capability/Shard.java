package no.vegvesen.ixn.federation.model.capability;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "shards", uniqueConstraints = @UniqueConstraint(columnNames = {"shardId", "exchangeName", "selector"}))
public class Shard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shard_seq")
    private Integer id;

    private Integer shardId;

    private String exchangeName;

    @Column(columnDefinition="TEXT")
    private String selector;

    public Shard() {

    }

    public Shard(Integer shardId, String exchangeName, String selector) {
        this.shardId = shardId;
        this.exchangeName = exchangeName;
        this.selector = selector;
    }

    public Integer getShardId() {
        return shardId;
    }

    public void setShardId(Integer shardId) {
        this.shardId = shardId;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shard)) return false;
        Shard shard = (Shard) o;
        return shardId.equals(shard.shardId) && exchangeName.equals(shard.exchangeName) && selector.equals(shard.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardId, exchangeName, selector);
    }
}
