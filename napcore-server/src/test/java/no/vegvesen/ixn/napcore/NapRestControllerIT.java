package no.vegvesen.ixn.napcore;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.DeliveryPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class NapRestControllerIT extends PostgresContainerBase {

    @Autowired
    private  ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private  NeighbourRepository neighbourRepository;

    @MockBean
    private  CertService certService;

    @MockBean
    private CertSigner certSigner;

    @Autowired
    private  NapCoreProperties napCoreProperties;

    @Autowired
    private NapRestController napRestController;

    @Test
    public void objectsAreAutowired(){
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(neighbourRepository).isNotNull();
        assertThat(napCoreProperties).isNotNull();
        assertThat(napRestController).isNotNull();
    }

    @Test
    public void testAddSubscriptionWithNullObjectThrowsException() {
        String actorCommonName = "actor";
        assertThrows(SubscriptionRequestException.class, () -> napRestController.addSubscription(actorCommonName, null));
    }

    @Test
    public void testAddingSubscriptionWithInvalidSelectorSetsStatusToIllegal(){
        String actorCommonName = "actor";
        Subscription subscription = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("1=1"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ILLEGAL);
    }

    @Test
    public void testAddingSubscriptionWithNullSelectorThrowsException(){
        String actorCommonName = "actor";
        assertThrows(SubscriptionRequestException.class, () -> napRestController.addSubscription(actorCommonName, new SubscriptionRequest()));
    }

    @Test
    public void testAddingSubscriptionWithValidSelectorReturnsValidSubscriptionAndCreatesServiceProvider(){
        String actorCommonName = "actor";
        Subscription subscription = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'"));
        System.out.println(subscription);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(napRestController.getSubscriptions(actorCommonName)).hasSize(1);
    }

    @Test
    public void testAddingSubscriptionComment(){
        String actorCommonName = "actor";
        Subscription subscription = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'"));
        Subscription updated = napRestController.addSubscriptionComment(actorCommonName, new AddCommentRequest(subscription.getId(), "First subscription"));
        assertThat(updated.getComment()).isNotEqualTo(null);
    }

    @Test
    public void testGetSubscriptionsReturnsValidSubscriptions(){
        String actorCommonName = "actor";
        SubscriptionRequest request1 = new SubscriptionRequest("originatingCountry='NO'");
        SubscriptionRequest request2 = new SubscriptionRequest("originatingCountry='SE'");
        napRestController.addSubscription(actorCommonName, request1);
        napRestController.addSubscription(actorCommonName, request2);

        List<Subscription> subscriptionList = napRestController.getSubscriptions(actorCommonName);
        assertThat(subscriptionList).hasSize(2);
        subscriptionList.forEach(a->{
            assertThat(a.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
        });
    }

    @Test
    public void testGetSubscriptionReturnsValidSubscription(){
        String actorCommonName = "actor";
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'");
        Subscription subscription = napRestController.addSubscription(actorCommonName, request);
        Subscription response = napRestController.getSubscription(actorCommonName, subscription.getId());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
    }

    @Test
    public void testGetSubscriptionThrowsErrorWhenItDoesNotExist(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getSubscription(actorCommonName, "25"));
    }

    @Test
    public void testSubscriptionIsDeletedCorrectly(){
        String actorCommonName = "actor";
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'");
        Subscription subscription = napRestController.addSubscription("actor", request);
        napRestController.deleteSubscription(actorCommonName, subscription.getId().toString());
        assertThat(napRestController.getSubscriptions(actorCommonName).stream().findFirst().get().getStatus()).isEqualTo(SubscriptionStatus.NOT_VALID);
    }

    @Test
    public void testGettingSubscriptionsReturnsCorrectOrder() throws InterruptedException {
        String actorCommonName = "actor";
        SubscriptionRequest request1 = new SubscriptionRequest("originatingCountry='NO'");
        SubscriptionRequest request2 = new SubscriptionRequest("originatingCountry='SE'");
        SubscriptionRequest request3 = new SubscriptionRequest("originatingCountry='FI'");
        serviceProviderRepository.save(new ServiceProvider(actorCommonName));
        napRestController.addSubscription(actorCommonName, request1);
        TimeUnit.SECONDS.sleep(1);
        napRestController.addSubscription(actorCommonName, request2);
        TimeUnit.SECONDS.sleep(1);
        napRestController.addSubscription(actorCommonName, request3);

        List<Subscription> subscriptions = napRestController.getSubscriptions(actorCommonName);
        assertThat(subscriptions.get(0).getSelector()).isEqualTo(request3.getSelector());
        assertThat(subscriptions.get(1).getSelector()).isEqualTo(request2.getSelector());
        assertThat(subscriptions.get(2).getSelector()).isEqualTo(request1.getSelector());
    }
    @Test
    public void testDeleteNonExistentSubscriptionThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deleteSubscription(actorCommonName, "1"));
    }

    @Test
    public void testAddingDeliveryWithValidSelectorGivesRequestedDelivery(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("originatingCountry='NO'");
        Delivery response = napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    }

    @Test
    public void testAddingDeliveryWithInvalidSelectorGivesInvalidDelivery(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("1=1");
        Delivery response = napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.ILLEGAL);
    }

    @Test
    public void testAddingNullDeliveryThrowsException(){
        String actorCommonName = "actor";
        assertThrows(DeliveryPostException.class, () -> napRestController.addDelivery(actorCommonName, null));
    }

    @Test
    public void testAddingNullSelectorInDeliveryThrowsException(){
        String actorCommonName = "actor";
        assertThrows(DeliveryPostException.class, () -> napRestController.addDelivery(actorCommonName, new DeliveryRequest()));
    }

    @Test
    public void testAddingDeliveryComment(){
        String actorCommonName = "actor";
        Delivery delivery = napRestController.addDelivery(actorCommonName, new DeliveryRequest("originatingCountry='NO'"));
        Delivery updated = napRestController.addDeliveryComment(actorCommonName, new AddCommentRequest(delivery.getId(), "First delivery"));
        assertThat(updated.getComment()).isNotEqualTo(null);
    }

    @Test
    public void testGettingNonExistentDeliveryThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getDelivery(actorCommonName, "1"));
    }

    @Test
    public void testGettingDelivery(){
        String actorCommonName = "actor";
        String selector = "originatingCountry='NO'";
        Delivery response = napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector));
        Delivery delivery = napRestController.getDelivery(actorCommonName, response.getId());
        assertThat(delivery).isNotNull();
        assertThat(delivery.getSelector()).isEqualTo(selector);
    }

    @Test
    public void testGettingDeliveriesReturnsOrderedByLastUpdatedTimestamp() throws InterruptedException {
        String actorCommonName = "actor";
        String selector1 = "originatingCountry='NO'";
        String selector2 = "originatingCountry='SE'";
        String selector3 = "originatingCountry='FI'";
        ServiceProvider sp = new ServiceProvider(actorCommonName);
        sp = serviceProviderRepository.save(sp);
        napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector1));
        sp.getDeliveries().stream().filter(a->a.getSelector().equals(selector1)).forEach(a->a.setLastUpdatedTimestamp(LocalDateTime.now()));
        TimeUnit.SECONDS.sleep(1);
        napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector2));
        sp.getDeliveries().stream().filter(a->a.getSelector().equals(selector2)).forEach(a->a.setLastUpdatedTimestamp(LocalDateTime.now()));
        TimeUnit.SECONDS.sleep(1);
        napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector3));
        sp.getDeliveries().stream().filter(a->a.getSelector().equals(selector3)).forEach(a->a.setLastUpdatedTimestamp(LocalDateTime.now()));

        List<Delivery> deliveries = napRestController.getDeliveries(actorCommonName);
        assertThat(deliveries.get(0).getSelector()).isEqualTo(selector3);
        assertThat(deliveries.get(1).getSelector()).isEqualTo(selector2);
        assertThat(deliveries.get(2).getSelector()).isEqualTo(selector1);
    }

    @Test
    public void testGettingMatchingDeliveryCapabilities(){
        String actor1 = "actor-1";
        String actor2 = "actor-2";
        String selector = "originatingCountry='NO'";
        CapabilitiesRequest request1 = new CapabilitiesRequest(
          new DatexApplicationApi("pub-id","publication-id","NO","1", List.of("1"), "type","name"),
          new MetadataApi()
        );

        CapabilitiesRequest request2 = new CapabilitiesRequest(
                new DatexApplicationApi("pub-id","publication-id-2","SE","1", List.of("1"), "type","name"),
                new MetadataApi()
        );

        CapabilitiesRequest request3 = new CapabilitiesRequest(
                new DatexApplicationApi("pub-id","publication-id-3","NO","1", List.of("1"), "type","name"),
                new MetadataApi()
        );
        napRestController.addCapability(actor1, request1);
        napRestController.addCapability(actor1, request2);
        napRestController.addCapability(actor2, request3);

        List<no.vegvesen.ixn.napcore.model.Capability> response1 = napRestController.getMatchingDeliveryCapabilities(actor1, selector);
        List<no.vegvesen.ixn.napcore.model.Capability> response2 = napRestController.getMatchingDeliveryCapabilities(actor1, "originatingCountry='SE'");
        List<no.vegvesen.ixn.napcore.model.Capability> response3 = napRestController.getMatchingDeliveryCapabilities(actor2, "originatingCountry='SE'");
        List<no.vegvesen.ixn.napcore.model.Capability> response4 = napRestController.getMatchingDeliveryCapabilities(actor2, selector);
        assertThat(response1).hasSize(1);
        assertThat(response2).hasSize(1);
        assertThat(response3).hasSize(0);
        assertThat(response4).hasSize(1);
    }

    @Test
    public void testDeletingNonExistentDeliveryThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deleteDelivery(actorCommonName, "1"));
    }

    @Test
    public void testDeletingDelivery(){
        String actorCommonName = "actor";
        DeliveryRequest request = new DeliveryRequest("originatingCountry='NO'");
        Delivery delivery = napRestController.addDelivery(actorCommonName, request);

        napRestController.deleteDelivery(actorCommonName, delivery.getId());
        for(Delivery response : napRestController.getDeliveries(actorCommonName)){
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.ILLEGAL);
        }
    }

    @Test
    public void testAddingCapability(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("String publisherId", "String publicationId", "String originatingCountry", "String protocolVersion", List.of("1"), "test", "test"),
                new MetadataApi()
        );
        OnboardingCapability response = napRestController.addCapability(actorCommonName, capabilitiesRequest);
        assertThat(response).isNotNull();
    }

    @Test
    public void testAddingCapabilityWithInvalidQuadTreeThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("String publisherId", "String publicationId", "String originatingCountry", "String protocolVersion", List.of("124"), "test", "test"),
                new MetadataApi()
        );
        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingCapabilityWithMissingPropertiesThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("String publisherId", "String publicationId", "String originatingCountry", null, List.of("1"), "test", "test"),
                new MetadataApi()
        );
        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingCapabilityWithDuplicatePublicationIdThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("String publisherId", "String publicationId", "String originatingCountry", "protocolversion", List.of("1"), "test", "test"),
                new MetadataApi()
        );
        assertThat(napRestController.addCapability(actorCommonName, capabilitiesRequest)).isNotNull();

        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingNullCapabilityThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest request = null;

        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, request));
    }

    @Test
    public void testAddingCapabilityWithNullApplicationOrMetadataThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest request1 = new CapabilitiesRequest(new MapemApplicationApi(), null);
        CapabilitiesRequest request2 = new CapabilitiesRequest(null, new MetadataApi());

        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, request1));
        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, request2));
    }

    @Test
    public void testGettingNonExistentCapabilityThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getCapability(actorCommonName, "1"));
    }

    @Test
    public void testGettingCapabilityWithInvalidIdThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getCapability(actorCommonName, "notAnId"));
    }

    @Test
    public void testGettingCapabilities(){
        String actor1 = "actor";
        String actor2 = "actor-2";
        CapabilitiesRequest request = new CapabilitiesRequest(
                new DatexApplicationApi("pubId", "publi-Id", "originatingCountry", "protocolVersion", List.of("1"), "publicationtype", "publisherName"),
                new MetadataApi()
        );
        napRestController.addCapability(actor1, request);
        request.getApplication().setPublicationId("publi-Id-2");
        napRestController.addCapability(actor1, request);
        request.getApplication().setPublicationId("publi-Id-3");
        napRestController.addCapability(actor2, request);

        assertThat(napRestController.getCapabilities(actor1)).hasSize(2);
        assertThat(napRestController.getCapabilities(actor2)).hasSize(1);
    }

    @Test
    public void testDeletingCapability(){
        String actorCommonName = "actor";
        CapabilitiesRequest request = new CapabilitiesRequest(
                new DatexApplicationApi("pubId", "publi-Id", "originatingCountry", "protocolVersion", List.of("1"), "publicationtype", "publisherName"),
                new MetadataApi()
        );
        OnboardingCapability response = napRestController.addCapability(actorCommonName, request);
        napRestController.deleteCapability(actorCommonName, response.getId());

        for(Capability capability : serviceProviderRepository.findAll().stream().flatMap(a->a.getCapabilities().getCapabilities().stream()).collect(Collectors.toSet())){
            assertThat(capability.getStatus().equals(CapabilityStatus.TEAR_DOWN));
        }
    }

    @Test
    public void testDeletingNonExistentCapabilityThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deleteCapability(actorCommonName, "1"));
    }

    @Test
    public void testDeletingCapabilityWithInvalidIdThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deleteCapability(actorCommonName, "notAnId"));
    }

    @Autowired
    WebApplicationContext context;
    @Test
    public void genSwagger() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andDo((result -> {
                    Files.deleteIfExists(Paths.get("target/swagger/swagger.json"));
                    Files.createDirectories(Paths.get("target/swagger"));
                    try(FileWriter fileWriter = new FileWriter("target/swagger/swagger.json")){
                        fileWriter.write(result.getResponse().getContentAsString());
                    }

                }));
    }

}
