import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.serviceprovider.model.ListCapabilitiesResponse;
import no.vegvesen.ixn.serviceprovider.model.ListDeliveriesResponse;
import no.vegvesen.ixn.serviceprovider.model.ListSubscriptionsResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.impl.client.HttpClients;


import javax.net.ssl.SSLContext;
import java.util.List;

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

    public List<Neighbour> getAllNeighbours() {
        return null;
    }
    //Krav 3.1
    public ListCapabilitiesResponse getNeighbourCapabilities(String neighbourName) {
        String url = String.format("%s/%s/%s/capabilities/", server, user, neighbourName);
        return restTemplate.getForEntity(url, ListCapabilitiesResponse.class).getBody();
    }

    //Krav 3.2
    public ListSubscriptionsResponse getNeighbourSubscriptions(String neighbourName) {
        String url = String.format("%s/%s/%s/subscriptions/", server, user, neighbourName);
        return restTemplate.getForEntity(url, ListSubscriptionsResponse.class).getBody();
    }

    //mangler krav 3.3

    //Krav 4
    public getAllServiceProvidersResponse getAllServiceProviders(String serviceProvider) {
        String url = String.format("%s/%s/%s/", server, user, serviceProvider);
        return restTemplate.getForEntity(url, getAllServiceProvidersResponse.class).getBody();
    }

    //Krav 4.1
    //Denne må sees på
    public ListCapabilitiesResponse getServiceProviderCapabilities(String serviceProvider) {
        String url = String.format("%s/%s/%s/capabilities/", server, user, serviceProvider);
        return restTemplate.getForEntity(url, ListCapabilitiesResponse.class).getBody();
    }

    //Krav 4.2
    public ListSubscriptionsResponse getServiceProviderSubscriptions(String serviceProvider) {
        String url = String.format("%s/%s/%s/subscriptions/", server, user, serviceProvider);
        return restTemplate.getForEntity(url, ListSubscriptionsResponse.class).getBody();
    }

    //Krav 4.3
    //Denne må sees på
    public ListDeliveriesResponse listServiceProviderDeliveries(String serviceProvider) {
        String url = String.format("%s/%s/%s/deliveries",server, user, serviceProvider);
        return restTemplate.getForEntity(url,ListDeliveriesResponse.class).getBody();
    }
}
