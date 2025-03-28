package no.vegvesen.ixn.federation.adminserver.model.neighbour;

import java.util.List;

public class SubscriptionRequestApi {

    private Integer subreq_id;

    private List<SubscriptionApi> subscriptions;

    private Long successfulRequest;

    public SubscriptionRequestApi() {
    }

    public SubscriptionRequestApi(Integer subreq_id, List<SubscriptionApi> subscriptions, Long successfulRequest) {
        this.subreq_id = subreq_id;
        this.subscriptions = subscriptions;
        this.successfulRequest = successfulRequest;
    }

    public Integer getSubreq_id() {
        return subreq_id;
    }

    public void setSubreq_id(Integer subreq_id) {
        this.subreq_id = subreq_id;
    }

    public List<SubscriptionApi> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<SubscriptionApi> subscriptions) {
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
        return "SubscriptionRequestApi{" +
                "subreq_id=" + subreq_id +
                ", subscriptions=" + subscriptions +
                ", successfulRequest=" + successfulRequest +
                '}';
    }
}
