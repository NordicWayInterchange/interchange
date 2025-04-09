package no.vegvesen.ixn.federation.adminserver.qpid;

public class CapabilitiesLinkedDeliveryApi {

    String deliveryId;

    CapabilityMatchApi capabilityMatchApi;


    public CapabilitiesLinkedDeliveryApi(String deliveryId, CapabilityMatchApi capabilityMatchApi) {
        this.deliveryId = deliveryId;
        this.capabilityMatchApi = capabilityMatchApi;
    }

    public void setDeliveryId(String id) {
        this.deliveryId = deliveryId;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setCapabilityMatch(CapabilityMatchApi capabilityMatchApi) {
        this.capabilityMatchApi = capabilityMatchApi;
    }

    public CapabilityMatchApi getCapabilityMatch() {
        return capabilityMatchApi;
    }

    public String toString() {
        return "CapabilitiesLinkedDeliveryApi{" +
                "deliveryId=" + deliveryId +
                ", capabilityMatchApi=" + capabilityMatchApi +
                '}';
    }
}
