package no.vegvesen.ixn.federation.server;


import no.vegvesen.ixn.federation.api.v1_0.RequestedSubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.subscription.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.ServiceProviderService;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
public class NeighbourRestControllerIT extends PostgresContainerBase {

    @Autowired
    NeighbourRestController neighbourRestController;

    @MockitoBean
    CertService certService;

    @Autowired
    InterchangeNodeProperties interchangeNodeProperties;

    @Autowired
    ServiceProviderService serviceProviderService;

    @Autowired
    NeighbourService neighbourService;

    @Autowired
    NeighbourRepository neighbourRepository;

    @Autowired
    WebApplicationContext context;

    @Test
    public void requestSubscriptionsDoesNotIncludeTimeStamp(){
        Neighbour neighbour = new Neighbour("neighbour1", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest());
        neighbourRepository.save(neighbour);
        SubscriptionRequestApi request = new SubscriptionRequestApi(neighbour.getName(), Set.of(new RequestedSubscriptionApi(
                "originatingCountry='NO'",
                "sp.bouvetinterchange.eu"
        )));
        SubscriptionResponseApi response = neighbourRestController.requestSubscriptions(request);
        assertThat(response.toString().toLowerCase()).doesNotContain("lastupdatedtimestamp");
    }

    @Test
    public void pollSubscriptionIncludesTimestamp(){

        String uuid = UUID.randomUUID().toString();
        NeighbourSubscription subscription = new NeighbourSubscription(
                uuid,
                NeighbourSubscriptionStatus.CREATED,
                "1=1",
                "/subscriptions/" + uuid,
                "neighbour2",
                Set.of()
        );
        subscription.setLastUpdatedTimestamp(Instant.now().toEpochMilli());
        NeighbourSubscriptionRequest request = new NeighbourSubscriptionRequest(Set.of(subscription));
        Neighbour neighbour = new Neighbour("neighbour2", new NeighbourCapabilities(), request, new SubscriptionRequest());
        neighbour = neighbourRepository.save(neighbour);

        SubscriptionPollResponseApi responseApi = neighbourRestController.pollSubscription(neighbour.getName(), uuid);
        assertThat(responseApi.getLastUpdatedTimestamp()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void listSubscriptionsDoesNotIncludeTimestamp(){
        NeighbourSubscriptionRequest request = new NeighbourSubscriptionRequest(Set.of(new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED, "neighbour3")));
        Neighbour neighbour = new Neighbour("neighbour3", new NeighbourCapabilities(), request, new SubscriptionRequest());
        neighbour = neighbourRepository.save(neighbour);

        assertThat(neighbourRestController.listSubscriptions(neighbour.getName()).toString().toLowerCase()).doesNotContain("lastupdatedtimestamp");
    }

    @Test
    public void genSwagger() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo((result -> {
                    System.out.println(result.getResponse().getContentAsString());
                    Files.deleteIfExists(Paths.get("target/swagger/swagger.json"));
                    Files.createDirectories(Paths.get("target/swagger"));
                    try(FileWriter fileWriter = new FileWriter("target/swagger/swagger.json")){
                        fileWriter.write(result.getResponse().getContentAsString());
                    }

                }));
    }

}
