package no.vegvesen.ixn.serviceprovider.model;

import java.util.ArrayList;
import java.util.List;

public class LocalSubscriptionListApi {
    private List<LocalSubscriptionApi> subscritions = new ArrayList<>();

    public LocalSubscriptionListApi() {

    }

    public LocalSubscriptionListApi(List<LocalSubscriptionApi> subscritions) {
        this.subscritions = subscritions;
    }

    public List<LocalSubscriptionApi> getSubscritions() {
        return subscritions;
    }
}
