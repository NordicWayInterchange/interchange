package no.vegvesen.ixn.admin;

public class ServiceProviderApi {
    private String name;

    private String id;

    private String numberOfCapabilities;

    private String numberOfDeliveries;

    private String numberOfSubscriptions;

    private ServiceProviderStatusApi status;

    public ServiceProviderApi() {

    }
    public ServiceProviderApi(String name, String id, String numberOfCapabilities, String numberOfDeliveries, String numberOfSubscriptions, ServiceProviderStatusApi status) {
        this.name = name;
        this.id = id;
        this.numberOfCapabilities = numberOfCapabilities;
        this.numberOfDeliveries = numberOfDeliveries;
        this.numberOfSubscriptions = numberOfSubscriptions;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getNumberOfCapabilities() {
        return numberOfCapabilities;
    }

    public String getNumberOfDeliveries() {
        return numberOfDeliveries;
    }

    public String getNumberOfSubscriptions() {
        return numberOfSubscriptions;
    }

    public ServiceProviderStatusApi getStatus() {
        return status;
    }
}
