package no.vegvesen.ixn.federation.discoverer.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

public class NeighbourRESTClientTest {



    @Test
    public void pollSubscriptionStatus404() {
        String url = "https://myserver.eu/subscriptions/1";
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(MockRestRequestMatchers.requestTo(url))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));
        NeighbourRESTClient client = new NeighbourRESTClient(restTemplate,new ObjectMapper());
        Assertions.assertThrows(SubscriptionNotFoundException.class, () -> {
            client.doPollSubscriptionStatus(url,"test");
        });

    }



}
