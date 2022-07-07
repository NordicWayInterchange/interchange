import no.vegvesen.ixn.federation.model.ServiceProvider;

public class ServiceProviderApi {
    private String name;

    private String id;

    private String numberOfCapabilities;

    private String numberOfDeliveries;

    private String numberOfSubcriptions;

    private ServiceProviderStatusApi status;

    public ServiceProviderApi(String name, String id, String numberOfCapabilities, String numberOfDeliveries, String numberOfSubcriptions, ServiceProviderStatusApi status) {
        this.name = name;
        this.id = id;
        this.numberOfCapabilities = numberOfCapabilities;
        this.numberOfDeliveries = numberOfDeliveries;
        this.numberOfSubcriptions = numberOfSubcriptions;
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

    public String getNumberOfSubcriptions() {
        return numberOfSubcriptions;
    }

    public ServiceProviderStatusApi getStatus() {
        return status;
    }
}
