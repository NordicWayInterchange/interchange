package no.vegvesen.ixn.serviceprovider.client;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.List;

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

    public LocalCapability addCapability(CapabilityApi capability) {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CapabilityApi> entity = new HttpEntity<>(capability,headers);
        return restTemplate.exchange(server + "/" + user + "/capabilities", HttpMethod.POST, entity, LocalCapability.class).getBody();
    }

    public LocalCapabilityList getServiceProviderCapabilities() {
        return restTemplate.getForEntity(server + "/" + user + "/capabilities", LocalCapabilityList.class).getBody();
    }


    public LocalSubscriptionListApi getServiceProviderSubscriptions() {
		String url = String.format("%s/%s/subscriptions/", server, user);
		return restTemplate.getForEntity(url, LocalSubscriptionListApi.class).getBody();
    }

    public void deleteSubscriptions(Integer localSubscriptionId) {
        restTemplate.delete(String.format("%s/%s/subscriptions/%s", server, user, localSubscriptionId));
    }

    public LocalSubscriptionApi addSubscription(SelectorApi subscription) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SelectorApi> entity = new HttpEntity<>(subscription,headers);
		String url = String.format("/%s/subscriptions", user) ;
		return restTemplate.exchange(server + url, HttpMethod.POST, entity, LocalSubscriptionApi.class).getBody();
    }

    public void deleteCapability(Integer capabilityId) {
		restTemplate.delete(String.format("%s/%s/capabilities/%s", server, user, capabilityId));
    }

    public LocalSubscriptionApi getSubscription(Integer localSubscriptionId) {
        String url = String.format("%s/%s/subscriptions/%s", server, user, localSubscriptionId.toString());
        return restTemplate.getForEntity(url, LocalSubscriptionApi.class).getBody();
    }

    public PrivateChannelApi addPrivateChannel(PrivateChannelApi privateChannelApi) {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PrivateChannelApi> entity = new HttpEntity<>(privateChannelApi,headers);
        return restTemplate.exchange(server + "/" + user + "/privatechannels", HttpMethod.POST, entity, PrivateChannelApi.class).getBody();
    }

    public PrivateChannelListApi getPrivateChannels() {
        String url = String.format("%s/%s/privatechannels/", server, user);
        return restTemplate.getForEntity(url, PrivateChannelListApi.class).getBody();
    }

    public void deletePrivateChannel(Integer privateChannelId) {

    }
}


