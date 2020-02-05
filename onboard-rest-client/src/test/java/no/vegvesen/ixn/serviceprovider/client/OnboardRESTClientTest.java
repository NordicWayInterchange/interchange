package no.vegvesen.ixn.serviceprovider.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OnboardRESTClientTest {

    /*
    TODO
    This is an ad-hoc implementation of the client and test. This will be refactored into something a bit more
    usable when we have more experience in the usage of the client.
    Also, the actual test code should probablywork against testcontainers...
     */

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;


    @Ignore
    @Test
    public void testGetSubscriptions() throws JsonProcessingException {
        OnboardRESTClient restClient = new OnboardRESTClient(restTemplate);

        SubscriptionRequestApi subscriptionRequest = restClient.getServiceProviderSubscriptionRequest("https://bouvet-one.bouvetinterchange.no:4141/subscription/sp-one.bouvetinterchange.no");
        System.out.println(objectMapper.writeValueAsString(subscriptionRequest));
        /*
        {"name":"sp-one.bouvetinterchange.no","subscriptions":[{"selector":"messageType = 'DATEX2'","path":null,"quadTreeTiles":["123122"],"status":"CREATED"},{"selector":"messageType = 'DATEX2' and originatingCountry = 'NO'","path":null,"quadTreeTiles":[],"status":"CREATED"}]}
         */
    }

    @Ignore
    @Test
    public void testPostSubscription() throws JsonProcessingException {

        /*
        {"selector":"messageType = 'DATEX2'","path":null,"quadTreeTiles":["123122"],"status":"CREATED"

         */
        SubscriptionApi subscriptionApi = new SubscriptionApi();
        subscriptionApi.setQuadTreeTiles(Collections.singleton("123122"));
        subscriptionApi.setSelector("messageType = 'DATEX2'");
        SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
        subscriptionRequestApi.setName("sp-one.bouvetinterchange.no");
        subscriptionRequestApi.setSubscriptions(Collections.singleton(subscriptionApi));

        OnboardRESTClient client = new OnboardRESTClient(restTemplate);
        SubscriptionRequestApi result = client.addSubscriptions("https://bouvet-one.bouvetinterchange.no:4141/subscription",subscriptionRequestApi);
        System.out.println(objectMapper.writeValueAsString(result));
    }


    @Ignore
    @Test
    public void testDeleteSubscription() throws JsonProcessingException {
        SubscriptionApi subscriptionApi = new SubscriptionApi();
        subscriptionApi.setSelector("messageType = 'DATEX2'");
        subscriptionApi.setQuadTreeTiles(Collections.singleton("123122"));

        SubscriptionRequestApi subscriptionRequest = new SubscriptionRequestApi();
        subscriptionRequest.setName("sp-one.bouvetinterchange.no");
        subscriptionRequest.setSubscriptions(Collections.singleton(subscriptionApi));

        OnboardRESTClient restClient = new OnboardRESTClient(restTemplate);
        SubscriptionRequestApi updated = restClient.deleteSubscriptions("https://bouvet-one.bouvetinterchange.no:4141/subscription",subscriptionRequest);
        System.out.println(objectMapper.writeValueAsString(updated));
        /*
        {"name":"sp-one.bouvetinterchange.no","subscriptions":[{"selector":"messageType = 'DATEX2' and originatingCountry = 'NO'","path":null,"quadTreeTiles":[],"status":"CREATED"}]}
         */
    }

    @Ignore
    @Test
    public void testGetCapabilities() throws JsonProcessingException {
        OnboardRESTClient restClient = new OnboardRESTClient(restTemplate);

        CapabilityApi capabilities = restClient.getServiceProviderCapabilities("https://bouvet-one.bouvetinterchange.no:4141/capabilities/sp-one.bouvetinterchange.no");
        System.out.println(objectMapper.writeValueAsString(capabilities));
        /*
        {"version":"1.0","name":"sp-one.bouvetinterchange.no","capabilities":[{"messageType":"DATEX2","originatingCountry":"SE","quadTree":[],"publicationSubType":[]},{"messageType":"DATEX2","originatingCountry":"NO","quadTree":[],"publicationSubType":[]}]}
         */
    }


    @Ignore
    @Test
    public void testDeleteCapability() throws JsonProcessingException {
        OnboardRESTClient restClient = new OnboardRESTClient(restTemplate);

        DataTypeApi dataTypeApi = new Datex2DataTypeApi();
        dataTypeApi.setOriginatingCountry("NO");

        CapabilityApi capabilityApi = new CapabilityApi();
        capabilityApi.setName("sp-one.bouvetinterchange.no");
        capabilityApi.setCapabilities(Collections.singleton(dataTypeApi));

        CapabilityApi result = restClient.deleteCapability("https://bouvet-one.bouvetinterchange.no:4141/capabilities",capabilityApi);
        System.out.println(objectMapper.writeValueAsString(result));
        /*
        {"version":"1.0","name":"sp-one.bouvetinterchange.no","capabilities":[{"messageType":"DATEX2","originatingCountry":"SE","quadTree":[],"publicationSubType":[]}]}
        */
    }

    @Ignore
    @Test
    public void testPostCapabilities() throws JsonProcessingException {
        OnboardRESTClient restClient = new OnboardRESTClient(restTemplate);

        DataTypeApi dataTypeApi = new Datex2DataTypeApi();
        dataTypeApi.setOriginatingCountry("NO");

        CapabilityApi capabilityApi = new CapabilityApi();
        capabilityApi.setName("sp-one.bouvetinterchange.no");
        capabilityApi.setCapabilities(Collections.singleton(dataTypeApi));

        CapabilityApi result = restClient.postCapabilities("https://bouvet-one.bouvetinterchange.no:4141/capabilities",capabilityApi);
        System.out.println(objectMapper.writeValueAsString(result));
        /*
        {"version":"1.0","name":"sp-one.bouvetinterchange.no","capabilities":[{"messageType":"DATEX2","originatingCountry":"SE","quadTree":[],"publicationSubType":[]},{"messageType":"DATEX2","originatingCountry":"NO","quadTree":[],"publicationSubType":[]}]}
         */
    }

}
