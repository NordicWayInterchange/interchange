package no.vegvesen.ixn.federation.adminserver.model;

public class SubscriptionShardApi {

    private String exchangeName;

    public SubscriptionShardApi() {
    }

    public SubscriptionShardApi(String exchangeName) {
        this.exchangeName = exchangeName;
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
                "exchangeName='" + exchangeName + '\'' +
                '}';
    }
}
