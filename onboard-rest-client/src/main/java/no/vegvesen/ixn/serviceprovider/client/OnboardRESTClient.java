package no.vegvesen.ixn.serviceprovider.client;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

public class OnboardRESTClient {


    private RestTemplate restTemplate;
    private final String server;
    private final String user;

    public OnboardRESTClient(SSLContext sslContext, String server, String user) {
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

    public CapabilityApi addCapabilities(CapabilityApi capabilities) {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CapabilityApi> entity = new HttpEntity<>(capabilities,headers);
        return restTemplate.exchange(server + "/capabilities", HttpMethod.POST, entity, CapabilityApi.class).getBody();
    }

    public CapabilityApi getServiceProviderCapabilities() {
        return restTemplate.getForEntity(server + "/capabilities/" + user, CapabilityApi.class).getBody();
    }


    public SubscriptionRequestApi getServiceProviderSubscriptionRequest() {
        return restTemplate.getForEntity(server + "/subscription/" + user,SubscriptionRequestApi.class).getBody();
    }

    public SubscriptionRequestApi deleteSubscriptions(SubscriptionRequestApi subscriptionRequest) {
        HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequest);
        return restTemplate.exchange(server + "/subscription", HttpMethod.DELETE, entity, SubscriptionRequestApi.class).getBody();
    }

    public SubscriptionRequestApi addSubscriptions(SubscriptionRequestApi subscriptionRequestApi) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequestApi,headers);
        return restTemplate.exchange(server + "/subscription", HttpMethod.POST, entity, SubscriptionRequestApi.class).getBody();
    }

    public CapabilityApi deleteCapability(CapabilityApi capabilityApi) {
        HttpEntity<CapabilityApi> entity = new HttpEntity<>(capabilityApi);
        return restTemplate.exchange(server + "/capabilities" ,HttpMethod.DELETE ,entity, CapabilityApi.class).getBody();
    }
}


