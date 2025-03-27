package no.vegvesen.ixn.federation.adminserver.model.neighbour;

import java.util.List;

public class NeighbourSubscriptionRequestApi {

    private Integer id;

    private List<NeighbourSubscriptionApi> subscriptions;

    private Long successfulRequest;

    public NeighbourSubscriptionRequestApi() {
    }

    public NeighbourSubscriptionRequestApi(Integer id, List<NeighbourSubscriptionApi> subscriptions, Long successfulRequest) {
        this.id = id;
        this.subscriptions = subscriptions;
        this.successfulRequest = successfulRequest;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<NeighbourSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<NeighbourSubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Long getSuccessfulRequest() {
        return successfulRequest;
    }

    public void setSuccessfulRequest(Long successfulRequest) {
        this.successfulRequest = successfulRequest;
    }

    @Override
    public String toString() {
        return "NeighbourSubscriptionRequestApi{" +
                "id=" + id +
                ", subscriptions=" + subscriptions +
                ", successfulRequest=" + successfulRequest +
                '}';
    }
}
