package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "endpoints", uniqueConstraints = @UniqueConstraint(columnNames = {"source","host","port"}, name = "uc_endpoint"))
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "endpoint_seq")
    @Column(name = "id")
    private Integer id;

    private String source;
    private String host;
    private Integer port;
    private Integer maxBandwidth;
    private Integer maxMessageRate;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "subshard_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_sub_shard"))
    private SubscriptionShard shard;

    public Endpoint() {

    }

    public Endpoint(Integer id, String source, String host, Integer port, Integer maxMessageRate, Integer maxBandwidth) {
        this.id = id;
        this.source = source;
        this.host = host;
        this.port = port;
        this.maxMessageRate = maxMessageRate;
        this.maxBandwidth = maxBandwidth;
    }

    public Endpoint(String source, String host, Integer port) {
        this(null,source,host,port,null,null);
    }

    public Endpoint(String source, String host, Integer port, Integer maxBandwidth, Integer maxMessageRate) {
        this(null,source,host,port,maxMessageRate,maxBandwidth);
    }

    public Endpoint(String source, String host, Integer port, SubscriptionShard shard) {
        this(null,source,host,port,null,null);
        this.setShard(shard);
    }

    public Endpoint(Integer id, String source, String host, Integer port) {
        this(id,source,host,port,null,null);
    }

    public String getSource() {
        return source;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getMaxBandwidth() {
        return maxBandwidth;
    }

    public void setMaxBandwidth(Integer maxBandwidth) {
        this.maxBandwidth = maxBandwidth;
    }

    public Integer getMaxMessageRate() {
        return maxMessageRate;
    }

    public void setMaxMessageRate(Integer maxMessageRate) {
        this.maxMessageRate = maxMessageRate;
    }

    public Integer getId() {
        return id;
    }

    public SubscriptionShard getShard() {
        return shard;
    }

    public void setShard(SubscriptionShard newShard) {
        this.shard = newShard;
    }

    public boolean hasShard() {
        return shard != null;
    }

    public void removeShard() {
        this.shard = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Endpoint)) return false;
        Endpoint endpoint = (Endpoint) o;
        return source.equals(endpoint.source) &&
                host.equals(endpoint.host) &&
                port.equals(endpoint.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, host, port);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", maxBandwidth=" + maxBandwidth +
                ", maxMessageRate=" + maxMessageRate +
                '}';
    }
}
