package no.vegvesen.ixn.federation.server;


import no.vegvesen.ixn.federation.api.v1_0.RequestedSubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.NeighbourSubscription;
import no.vegvesen.ixn.federation.model.NeighbourSubscriptionRequest;
import no.vegvesen.ixn.federation.model.NeighbourSubscriptionStatus;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.ServiceProviderService;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@SpringBootTest
public class NeighbourRestControllerIT {

    @Autowired
    NeighbourRestController neighbourRestController;

    @MockBean
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
    public void requestSubscriptionsReturnsUUID(){
        Neighbour neighbour = new Neighbour();
        neighbour.setName("neighbour_uuid_1");
        neighbourRepository.save(neighbour);
        SubscriptionRequestApi request = new SubscriptionRequestApi(neighbour.getName(), Set.of(
                new RequestedSubscriptionApi("originatingCountry='NO'")
        ));
        assertTrue(checkUuid(neighbourRestController.requestSubscriptions(request).getSubscriptions().stream().findAny().get().getId()));
    }

    @Test
    public void listSubscriptionsReturnsUUID(){
       Neighbour neighbour = new Neighbour();
       neighbour.setName("neighbour_uuid_2");
       neighbour.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(
               Set.of(new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED))
       ));
       neighbourRepository.save(neighbour);

       assertTrue(checkUuid(neighbourRestController.listSubscriptions(neighbour.getName()).getSubscriptions().stream().findFirst().get().getId()));
    }

    @Test
    public void pollSubscriptionReturnsUUID(){
        Neighbour neighbour = new Neighbour();
        neighbour.setName("neighbour_uuid_3");
        NeighbourSubscriptionRequest request = new NeighbourSubscriptionRequest(Set.of(
                new NeighbourSubscription("originatingCountry='NO'", NeighbourSubscriptionStatus.CREATED)
        ));
        neighbour.setNeighbourRequestedSubscriptions(request);
        neighbourRepository.save(neighbour);
        SubscriptionPollResponseApi responseApi = neighbourRestController.pollSubscription(neighbour.getName(), request.getSubscriptions().stream().findFirst().get().getUuid());
        assertTrue(checkUuid(responseApi.getId()));
    }

    private boolean checkUuid(String id){
        try{
            UUID.fromString(id);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
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
