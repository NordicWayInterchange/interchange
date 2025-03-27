package no.vegvesen.ixn.federation.adminserver.model.neighbour;

public class SubscriptionShardApi {

    private Integer id;

    private String exchangeName;

    public SubscriptionShardApi() {
    }

    public SubscriptionShardApi(Integer id, String exchangeName) {
        this.id = id;
        this.exchangeName = exchangeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    @Override
    public String toString() {
        return "SubscriptionShardApi{" +
                "id=" + id +
                ", exchangeName='" + exchangeName + '\'' +
                '}';
    }
}
