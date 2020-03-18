package no.vegvesen.ixn.serviceprovider.client;

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.serviceprovider.model.DataTypeApiId;
import no.vegvesen.ixn.serviceprovider.model.DataTypeIdList;
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

    public DataTypeApiId addCapability(DataTypeApi capability) {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DataTypeApi> entity = new HttpEntity<>(capability,headers);
        return restTemplate.exchange(server + "/" + user + "/capabilities", HttpMethod.POST, entity, DataTypeApiId.class).getBody();
    }

    public DataTypeIdList getServiceProviderCapabilities() {
        return restTemplate.getForEntity(server + "/" + user + "/capabilities", DataTypeIdList.class).getBody();
    }


    public DataTypeIdList getServiceProviderSubscriptionRequest() {
		String url = String.format("%s/%s/subscriptions/", server, user);
		return restTemplate.getForEntity(url, DataTypeIdList.class).getBody();
    }

    public void deleteSubscriptions(Integer localSubscriptionId) {
        restTemplate.delete(String.format("%s/%s/subscriptions/%s", server, user, localSubscriptionId));
    }

    public DataTypeApiId addSubscription(DataTypeApi subscription) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DataTypeApi> entity = new HttpEntity<>(subscription,headers);
		String url = String.format("/%s/subscriptions", user) ;
		return restTemplate.exchange(server + url, HttpMethod.POST, entity, DataTypeApiId.class).getBody();
    }

    public void deleteCapability(Integer capabilityId) {
		restTemplate.delete(String.format("%s/%s/capabilities/%s", server, user, capabilityId));
    }
}


