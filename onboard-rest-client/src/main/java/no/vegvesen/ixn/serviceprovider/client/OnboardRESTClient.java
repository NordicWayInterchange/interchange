package no.vegvesen.ixn.serviceprovider.client;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class OnboardRESTClient {


    private RestTemplate restTemplate;

    public OnboardRESTClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CapabilityApi postCapabilities(String url,CapabilityApi capabilities) {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CapabilityApi> entity = new HttpEntity<>(capabilities,headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, CapabilityApi.class).getBody();
    }

    public CapabilityApi getServiceProviderCapabilities(String uri) {
        return restTemplate.getForEntity(uri, CapabilityApi.class).getBody();
    }


    public SubscriptionRequestApi getServiceProviderSubscriptionRequest(String uri) {
        return restTemplate.getForEntity(uri,SubscriptionRequestApi.class).getBody();
    }

    public SubscriptionRequestApi deleteSubscriptions(String uri, SubscriptionRequestApi subscriptionRequest) {
        HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequest);
        return restTemplate.exchange(uri, HttpMethod.DELETE, entity, SubscriptionRequestApi.class).getBody();
    }

    public SubscriptionRequestApi addSubscriptions(String uri, SubscriptionRequestApi subscriptionRequestApi) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequestApi,headers);
        return restTemplate.exchange(uri, HttpMethod.POST, entity, SubscriptionRequestApi.class).getBody();
    }

    public CapabilityApi deleteCapability(String uri, CapabilityApi capabilityApi) {
        HttpEntity<CapabilityApi> entity = new HttpEntity<>(capabilityApi);
        return restTemplate.exchange(uri ,HttpMethod.DELETE ,entity, CapabilityApi.class).getBody();
    }
}


