package no.vegvesen.ixn.federation.adminserver.model;

public class InterchangeApi {

    private InterchangeCapabilityApi capabilities;

    private InterchangeSubscriptionApi subscriptions;

    private InterchangeDeliveryApi deliveries;


    public InterchangeApi(InterchangeCapabilityApi capabilities, InterchangeSubscriptionApi subscriptions, InterchangeDeliveryApi deliveries) {
        this.capabilities = capabilities;
        this.deliveries = deliveries;
    }

    public InterchangeApi() {

    }

    public InterchangeCapabilityApi getInterchangeCapabilityApi() {
        return capabilities;
    }

    public void setInterchangeCapabilityApi(InterchangeCapabilityApi capabilities) {
        this.capabilities = capabilities;
    }

    public InterchangeDeliveryApi getInterchangeDeliveryApi() {
        return deliveries;
    }

    public void setInterchangeDeliveryApi(InterchangeDeliveryApi deliveries) {
        this.deliveries = deliveries;
    }

    public InterchangeSubscriptionApi getInterchangeSubscriptionApi() {
        return subscriptions;
    }

    public void setInterchangeSubscriptionApi(InterchangeSubscriptionApi subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public String toString() {
        return "InterchangeApi{" +
                ", capabilities=" + capabilities +
                ", subscriptions=" + subscriptions +
                ", deliveries=" + deliveries +
                '}';
    }
}
