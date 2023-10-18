package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.model.Selector;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "shards", uniqueConstraints = @UniqueConstraint(columnNames = {"shardId", "exchangeName", "selector"}))
public class Shard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shard_seq")
    private Integer id;

    private Integer shardId;

    private String exchangeName;

    @Embedded
    private Selector selector;

    public Shard() {

    }

    public Shard(Integer shardId, String exchangeName, String selector) {
        this.shardId = shardId;
        this.exchangeName = exchangeName;
        this.selector = new Selector(selector);
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
        return selector.getSelector();
    }

    public void setSelector(String selector) {
        this.selector = new Selector(selector);
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
