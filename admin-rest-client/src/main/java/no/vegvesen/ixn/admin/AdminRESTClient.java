package no.vegvesen.ixn.admin;


import no.vegvesen.ixn.admin.GetAllServiceProvidersResponse;
import no.vegvesen.ixn.serviceprovider.model.ListCapabilitiesResponse;
import no.vegvesen.ixn.serviceprovider.model.ListDeliveriesResponse;
import no.vegvesen.ixn.serviceprovider.model.ListSubscriptionsResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.impl.client.HttpClients;


import javax.net.ssl.SSLContext;

public class AdminRESTClient {

    private RestTemplate restTemplate;
    private final String server;
    private final String user;

    public AdminRESTClient(SSLContext sslContext, String server, String user) {
        this.restTemplate = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(
                        HttpClients
                                .custom()
                                .setSSLContext(sslContext)
                                .build()
                )
        );
        this.server = server;
        this.user = user;
    }

    //TODO: Make response objects!
    //TODO: Check if paths are valid

    public GetAllNeighboursResponse getAllNeighbours() {
        String url = String.format("%s/%s/neighbour", server, user);
        return restTemplate.getForEntity(url, GetAllNeighboursResponse.class).getBody();
    }
    //Krav 3.1
    public ListNeighbourCapabilitiesResponse getNeighbourCapabilities(String neighbourName) {
        String url = String.format("%s/%s/neighbour/%s/capabilities", server, user, neighbourName);
        return restTemplate.getForEntity(url, ListNeighbourCapabilitiesResponse.class).getBody();
    }

    //Krav 3.2
    public ListNeighbourSubscriptionResponse getNeighbourSubscriptions(String neighbourName) {
        String url = String.format("%s/%s/neighbour/%s/subscriptions", server, user, neighbourName);
        return restTemplate.getForEntity(url, ListNeighbourSubscriptionResponse.class).getBody();
    }

    //mangler krav 3.3
    //Krav 3.2
    /*
    public boolean isNeighbourReachable(String neighbourName) {
        String url = String.format("%s/%s/neighbour/%s/isReachable", server, user, neighbourName);
        return restTemplate.getForEntity(url, isNeighbourReachable.class).getBody();
    }

     */

    //Krav 4
    public GetAllServiceProvidersResponse getAllServiceProviders() {
        String url = String.format("%s/%s/serviceProvider", server, user);
        return restTemplate.getForEntity(url, GetAllServiceProvidersResponse.class).getBody();
    }

    public GetServiceProviderResponse getServiceProvider(String serviceProvider) {
        String url = String.format("%s/%s/serviceProvider/%s", server, user, serviceProvider);
        return restTemplate.getForEntity(url, GetServiceProviderResponse.class).getBody();
    }

    //Krav 4.1
    //Denne må sees på
    public ListCapabilitiesResponse getServiceProviderCapabilities(String serviceProvider) {
        String url = String.format("%s/%s/serviceProvider/%s/capabilities/", server, user, serviceProvider);
        return restTemplate.getForEntity(url, ListCapabilitiesResponse.class).getBody();
    }

    //Krav 4.2
    public ListSubscriptionsResponse getServiceProviderSubscriptions(String serviceProvider) {
        String url = String.format("%s/%s/serviceProvider/%s/subscriptions/", server, user, serviceProvider);
        return restTemplate.getForEntity(url, ListSubscriptionsResponse.class).getBody();
    }

    //Krav 4.3
    //Denne må sees på
    public ListDeliveriesResponse listServiceProviderDeliveries(String serviceProvider) {
        String url = String.format("%s/%s/serviceProvider/%s/deliveries",server, user, serviceProvider);
        return restTemplate.getForEntity(url,ListDeliveriesResponse.class).getBody();
    }
}
