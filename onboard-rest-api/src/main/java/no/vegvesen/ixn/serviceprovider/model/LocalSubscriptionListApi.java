package no.vegvesen.ixn.serviceprovider.model;

import java.util.ArrayList;
import java.util.List;

public class LocalSubscriptionListApi {
    private List<LocalSubscriptionApi> subscriptions = new ArrayList<>();

    public LocalSubscriptionListApi() {

    }

    public LocalSubscriptionListApi(List<LocalSubscriptionApi> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<LocalSubscriptionApi> getSubscriptions() {
        return subscriptions;
    }
}
