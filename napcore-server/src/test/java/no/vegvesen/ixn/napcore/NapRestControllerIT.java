package no.vegvesen.ixn.napcore;

import jakarta.transaction.Transactional;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.docker.PostgresContainerBase;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import no.vegvesen.ixn.napcore.model.SubscriptionStatus;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private PrivateChannelRepository privateChannelRepository;

    @MockitoBean
    private CertService certService;

    @MockitoBean
    private CertSigner certSigner;

    @Autowired
    private NapCoreProperties napCoreProperties;

    @Autowired
    private NapRestController napRestController;

    @Test
    public void objectsAreAutowired(){
        assertThat(serviceProviderRepository).isNotNull();
        assertThat(neighbourRepository).isNotNull();
        assertThat(privateChannelRepository).isNotNull();
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
        Subscription subscription = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("1=1", "invalid sub"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ILLEGAL);
    }

    @Test
    public void testAddingSubscriptionWithNullSelectorThrowsException(){
        String actorCommonName = "actor";
        assertThrows(SubscriptionRequestException.class, () -> napRestController.addSubscription(actorCommonName, new SubscriptionRequest()));
    }

    @Test
    public void testAddingSubscriptionThatAlreadyExistsThrowsException(){
        String actorCommonName = "actor";
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'");
        napRestController.addSubscription(actorCommonName, request);
        assertThrows(AlreadyExistsException.class, () -> napRestController.addSubscription(actorCommonName, request));
    }

    @Test
    public void testAddingSubscriptionWithValidSelectorReturnsValidSubscriptionAndCreatesServiceProvider(){
        String actorCommonName = "actor";
        Subscription subscription = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'", "NO sub"));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.REQUESTED);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(napRestController.getSubscriptions(actorCommonName)).hasSize(1);
    }

    @Test
    public void testAddingSubscriptionWithoutDescription(){
        String actorCommonName = "actor";
        Subscription subscription1 = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'"));
        System.out.println(subscription1);
        assertThat(napRestController.getSubscriptions(actorCommonName)).hasSize(1);
    }

    @Test
    public void testGetSubscriptionsReturnsValidSubscriptions(){
        String actorCommonName = "actor";
        SubscriptionRequest request1 = new SubscriptionRequest("originatingCountry='NO'", "NO Sub");
        SubscriptionRequest request2 = new SubscriptionRequest("originatingCountry='SE'", "SE Sub");
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
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'", "NO SUb");
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
        SubscriptionRequest request = new SubscriptionRequest("originatingCountry='NO'", "No sub");
        Subscription subscription = napRestController.addSubscription("actor", request);
        napRestController.deleteSubscription(actorCommonName, subscription.getId().toString());
        assertThat(napRestController.getSubscriptions(actorCommonName).stream().findFirst().get().getStatus()).isEqualTo(SubscriptionStatus.NOT_VALID);
    }

    @Test
    public void testDeleteNonExistentSubscriptionThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deleteSubscription(actorCommonName, "1"));
    }

    @Test
    public void testAddingDeliveryWithValidSelectorGivesRequestedDelivery(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("originatingCountry='NO'", "NO delivery");
        Delivery response = napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    }

    @Test
    public void testAddingDeliveryThatAlreadyExistsThrowsException(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("originatingCountry='NO'", "NO delivery");
        napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThrows(AlreadyExistsException.class, () -> napRestController.addDelivery(actorCommonName, deliveryRequest));
    }
    @Test
    public void testAddingDeliveryWithInvalidSelectorGivesInvalidDelivery(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("1=1", "Invalid delivery");
        Delivery response = napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.ILLEGAL);
    }

    @Test
    public void testAddingDeliveryWithoutDescription(){
        String actorCommonName = "actor";
        DeliveryRequest deliveryRequest = new DeliveryRequest("originatingCountry='NO'");
        napRestController.addDelivery(actorCommonName, deliveryRequest);
        assertThat(napRestController.getDeliveries(actorCommonName)).hasSize(1);
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
    public void testGettingNonExistentDeliveryThrowsException(){
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getDelivery(actorCommonName, "1"));
    }

    @Test
    public void testGettingDelivery(){
        String actorCommonName = "actor";
        String selector = "originatingCountry='NO'";
        Delivery response = napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector, "NO Delivery"));
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
        napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector1, "Delivery 1"));
        sp.getDeliveries().stream().filter(a->a.getSelector().equals(selector1)).forEach(a->a.setLastUpdatedTimestamp(LocalDateTime.now()));
        TimeUnit.SECONDS.sleep(1);
        napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector2, "Delivery 2"));
        sp.getDeliveries().stream().filter(a->a.getSelector().equals(selector2)).forEach(a->a.setLastUpdatedTimestamp(LocalDateTime.now()));
        TimeUnit.SECONDS.sleep(1);
        napRestController.addDelivery(actorCommonName, new DeliveryRequest(selector3, "Delivery 3"));
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
          new DatexApplicationApi("DK12345","DK12345:publication-id","NO","1", List.of("1"), "type","name"),
          new MetadataApi()
        );

        CapabilitiesRequest request2 = new CapabilitiesRequest(
                new DatexApplicationApi("DK12345","DK12345:publication-id-2","SE","1", List.of("1"), "type","name"),
                new MetadataApi()
        );

        CapabilitiesRequest request3 = new CapabilitiesRequest(
                new DatexApplicationApi("DK12345","DK12345:publication-id-3","NO","1", List.of("1"), "type","name"),
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
        DeliveryRequest request = new DeliveryRequest("originatingCountry='NO'", "Test delivery");
        Delivery delivery = napRestController.addDelivery(actorCommonName, request);

        napRestController.deleteDelivery(actorCommonName, delivery.getId());
        for(Delivery response : napRestController.getDeliveries(actorCommonName)){
            assertThat(response.getStatus()).isEqualTo(DeliveryStatus.ILLEGAL);
        }
    }


    @Test
    public void testDeletingMultipleSubscriptions(){
        String actorCommonName = "actor";
        Subscription subscription1 = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'", "sub1"));

        Subscription subscription2 = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='SE'", "sub2"));

        Subscription subscription3 = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='FI'", "sub3"));
        assertThat(napRestController.getSubscriptions(actorCommonName)).hasSize(3);

        String multipleSubscriptionIds = subscription1.getId() + ',' + subscription2.getId() + ',' + subscription3.getId();
        napRestController.deleteSubscription(actorCommonName, multipleSubscriptionIds);
        for(Subscription response : napRestController.getSubscriptions(actorCommonName)){
            assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.NOT_VALID);
        }
    }

    @Test
    public void testDeletingOneExistingAndOneNonExistingSubscriptions(){
        String actorCommonName = "actor";
        Subscription subscription1 = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'", "sub1"));
        assertThat(napRestController.getSubscriptions(actorCommonName)).hasSize(1);

        String multipleSubscriptionIds = subscription1.getId() + ',' + "123";
        assertThrows(NotFoundException.class, () -> napRestController.deleteSubscription(actorCommonName, multipleSubscriptionIds));
    }

    @Test
    public void testDeletingMultipleNonExistingSubscriptions(){
        String actorCommonName = "actor";

        String multipleInvalidSubscriptionIds = "321" + ',' + "123";
        assertThrows(NotFoundException.class, () -> napRestController.deleteSubscription(actorCommonName, multipleInvalidSubscriptionIds));
    }

    @Test
    public void testDeletingSubscriptionsWithExtraCommas(){
        String actorCommonName = "actor";
        Subscription subscription1 = napRestController.addSubscription(actorCommonName, new SubscriptionRequest("originatingCountry='NO'", "sub1"));
        assertThat(napRestController.getSubscriptions(actorCommonName)).hasSize(1);

        String SubscriptionIdWithExtraCommas = subscription1.getId() + ',' + ',';
        napRestController.deleteSubscription(actorCommonName, SubscriptionIdWithExtraCommas);
        for(Subscription response : napRestController.getSubscriptions(actorCommonName)){
            assertThat(response.getStatus()).isEqualTo(SubscriptionStatus.NOT_VALID);
        }
    }

    @Test
    public void testAddingCapability(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("DK12345", "DK12345:publicationId", "NO", "protocolVersion", List.of("1"), "test", "test"),
                new MetadataApi()
        );
        OnboardingCapability response = napRestController.addCapability(actorCommonName, capabilitiesRequest);
        assertThat(response).isNotNull();
    }

    @Test
    public void testAddingCapabilityWithShardCountExceedingLimitThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("NO12345", "NO12345:1", "NO", "protocolVersion", List.of("1"), "test", "test"),
                new MetadataApi(11, "test", RedirectStatusApi.OPTIONAL, 1, 1, 1)
        );
        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingCapabilityWithShardCountWithinLimitDoesNotThrowException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("NO12345", "NO12345:1", "NO", "protocolVersion", List.of("1"), "test", "test"),
                new MetadataApi(9, "test", RedirectStatusApi.OPTIONAL, 1, 1, 1)
        );
        assertThat(napRestController.addCapability(actorCommonName, capabilitiesRequest)).isNotNull();
    }

    @Test
    public void testAddingCapabilityWithIllegalCharacterThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("NO12345", "NO12345:1'22", "NO", "protocolVersion", List.of("1"), "test", "test"),
                new MetadataApi()
        );
        assertThrows(CapabilityNotValidException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingCapabilityWithInvalidQuadTreeThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("NO12345", "NO12345:publicationId", "DK", "protocolVersion", List.of("124"), "test", "test"),
                new MetadataApi()
        );
        assertThrows(CapabilityNotValidException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingCapabilityWithMissingPropertiesThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("NO12345", "NO12345:1", "NO", null, List.of("1"), "test", "test"),
                new MetadataApi()
        );
        assertThrows(CapabilityPostException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingCapabilityWithDuplicatePublicationIdThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("DK12345", "DK12345:publicationId", "NO", "protocolversion", List.of("1"), "test", "test"),
                new MetadataApi()
        );
        assertThat(napRestController.addCapability(actorCommonName, capabilitiesRequest)).isNotNull();

        assertThrows(AlreadyExistsException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
    }

    @Test
    public void testAddingCapabilityWithInvalidPropertiesThrowsException(){
        String actorCommonName = "actor";
        CapabilitiesRequest capabilitiesRequest = new CapabilitiesRequest(
                new DatexApplicationApi("DK1234X", "DK12345:publicationId", "NO", "protocolVersion", List.of("1"), "test", "test"),
                new MetadataApi()
        );
        assertThrows(CapabilityNotValidException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
        capabilitiesRequest.getApplication().setPublisherId("DK12345");
        capabilitiesRequest.getApplication().setOriginatingCountry("NOK");
        assertThrows(CapabilityNotValidException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
        capabilitiesRequest.getApplication().setOriginatingCountry("NO");
        capabilitiesRequest.getApplication().setProtocolVersion("*!!!");
        assertThrows(CapabilityNotValidException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));
        capabilitiesRequest.getApplication().setProtocolVersion("protocolVersion");
        capabilitiesRequest.getApplication().setPublicationId("DK12345-publicationId");
        assertThrows(CapabilityNotValidException.class, () -> napRestController.addCapability(actorCommonName, capabilitiesRequest));

    }

    @Test
    public void testIllegalCharsInPathVariable(){
        String illegal1 = "s*";
        String legal = "s@_-.A0S5S";
        String illegal2 = "s#";
        String illegal3 = "s?";
        String illegal4 = "s/";
        String illegal5 = "s;";
        String illegal6 = "s!";
        String illegal7 = "s$";
        String illegal8 = "s&";
        String illegal9 = "s'";
        String illegal10 = "s(";
        String illegal11 = "s[";
        String illegal12 = "s{";
        String illegal13 = "s,";
        String illegal14 = "s=";

        DeliveryRequest request = new DeliveryRequest("test");
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal1, request));
        napRestController.addDelivery(legal, request);
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal2, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal3, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal4, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal5, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal6, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal7, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal8, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal9, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal10, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal11, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal12, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal13, request));
        assertThrows(PathVariableException.class, () -> napRestController.addDelivery(illegal14, request));
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
                new DatexApplicationApi("DK12345", "DK12345:id", "NO", "protocolVersion", List.of("1"), "publicationtype", "publisherName"),
                new MetadataApi()
        );
        napRestController.addCapability(actor1, request);
        request.getApplication().setPublicationId("DK12345:publi-Id-2");
        napRestController.addCapability(actor1, request);
        request.getApplication().setPublicationId("DK12345:publi-Id-3");
        napRestController.addCapability(actor2, request);

        ServiceProvider actor1Caps = serviceProviderRepository.findByName(actor1);
        for (Capability cap : actor1Caps.getCapabilities().getCapabilities()) {
            cap.setStatus(CapabilityStatus.CREATED);
        }
        serviceProviderRepository.save(actor1Caps);

        ServiceProvider actor2Caps = serviceProviderRepository.findByName(actor2);
        for (Capability cap : actor2Caps.getCapabilities().getCapabilities()) {
            cap.setStatus(CapabilityStatus.CREATED);
        }
        serviceProviderRepository.save(actor2Caps);

        assertThat(napRestController.getCapabilities(actor1)).hasSize(2);
        assertThat(napRestController.getCapabilities(actor2)).hasSize(1);
    }

    @Test
    public void testDeletingCapability(){
        String actorCommonName = "actor";
        CapabilitiesRequest request = new CapabilitiesRequest(
                new DatexApplicationApi("DK12345", "DK12345:Id", "NO", "protocolVersion", List.of("1"), "publicationtype", "publisherName"),
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

    @Test
    public void testAddingPrivateChannel() {
        String actorCommonName = "actor";
        PrivateChannelRequest request = new PrivateChannelRequest(Collections.singleton("peer"), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);
        assertThat(response).isNotNull();
    }

    @Test
    public void testAddingPrivateChannelWithRequestAsNull() {
        String actorCommonName = "actor";
        assertThrows(PrivateChannelException.class, () -> napRestController.addPrivateChannel(actorCommonName, null));
    }

    @Test
    public void testAddingPrivateChannelWithPeersListAsNull() {
        String actorCommonName = "actor";
        assertThrows(PrivateChannelException.class, () -> napRestController.addPrivateChannel(actorCommonName, new PrivateChannelRequest(null, "desc")));
    }

    @Test
    public void testAddingPrivateChannelWithEmptyPeersList() {
        String actorCommonName = "actor";
        assertThat(napRestController.addPrivateChannel(actorCommonName, new PrivateChannelRequest(Set.of(), "my private channel"))).isNotNull();
    }

    @Test
    public void testDeletingPrivateChannel() {
        String actorCommonName = "actor";
        PrivateChannelRequest request = new PrivateChannelRequest(Collections.singleton("peer"), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);
        napRestController.deletePrivateChannel(actorCommonName, response.getId());

        assertThat(privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId()).getStatus()).isEqualTo(no.vegvesen.ixn.federation.model.PrivateChannelStatus.TEAR_DOWN);
    }

    @Test
    public void testDeletingPrivateChannelWithNonExistingId() {
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deletePrivateChannel(actorCommonName, "notAnId"));
    }

    @Test
    public void testListingPrivateChannels() {
        String actorCommonName = "actor";
        PrivateChannelRequest request1 = new PrivateChannelRequest(Collections.singleton("peer1"), "My first private channel");
        PrivateChannelRequest request2 = new PrivateChannelRequest(Collections.singleton("peer2"), "My second private channel");

        napRestController.addPrivateChannel(actorCommonName, request1);
        napRestController.addPrivateChannel(actorCommonName, request2);

        List<PrivateChannelResponse> response = napRestController.getPrivateChannels(actorCommonName);
        assertThat(response).hasSize(2);
    }

    @Test
    public void testGettingPrivateChannelById() {
        String actorCommonName = "actor";
        PrivateChannelRequest request = new PrivateChannelRequest(Collections.singleton("peer"), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);

        PrivateChannelResponse getPrivateChannel = napRestController.getPrivateChannel(actorCommonName, response.getId());
        assertThat(getPrivateChannel).isNotNull();
    }

    @Test
    public void testGettingPrivateChannelWithNonExistingId() {
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.getPrivateChannel(actorCommonName, "notAnId"));
    }

    @Test
    public void testListingPrivateChannelsForPeer() {
        String actorCommonName = "actor";
        String peerName = "peer";
        PrivateChannelRequest request = new PrivateChannelRequest(Collections.singleton(peerName), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);

        List<PeerPrivateChannel> peerChannels = napRestController.getPeerPrivateChannels(peerName);
        Set<String> owners = peerChannels.stream().map(PeerPrivateChannel::getOwner).collect(Collectors.toSet());
        assertThat(owners).contains(actorCommonName);
    }

    @Test
    public void testGettingPrivateChannelForPeer() {
        String actorCommonName = "actor";
        String peerName = "peer";
        PrivateChannelRequest request = new PrivateChannelRequest(Collections.singleton(peerName), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);

        PeerPrivateChannel peerChannel = napRestController.getPeerPrivateChannel(peerName, response.getId());
        assertThat(peerChannel.getOwner()).isEqualTo(actorCommonName);
    }

    @Test
    public void testGettingPrivateChannelForPeerWithNonExistingId() {
        String peerName = "peer";
        assertThrows(NotFoundException.class, () -> napRestController.getPeerPrivateChannel(peerName, "notAnId"));
    }

    @Test
    public void testAddingPeerToPrivateChannel() {
        String actorCommonName = "actor";
        PrivateChannelRequest request = new PrivateChannelRequest(Collections.singleton("peer"), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);
        PrivateChannel savedChannel = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId());
        savedChannel.setStatus(no.vegvesen.ixn.federation.model.PrivateChannelStatus.CREATED);
        privateChannelRepository.save(savedChannel);

        AddPeerRequest newPeer = new AddPeerRequest("newPeer");

        napRestController.addPeerToPrivateChannel(actorCommonName, response.getId(), newPeer);
        Set<String> peers = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId()).getPeers().stream().map(Peer::getName).collect(Collectors.toSet());
        assertThat(peers).hasSize(2);
        assertThat(peers).contains("newPeer");
    }

    @Test
    public void testAddingPeerToPrivateChannelWithRequestAsNull() {
        String actorCommonName = "actor";
        assertThrows(PrivateChannelException.class, () -> napRestController.addPeerToPrivateChannel(actorCommonName, "validId", null));
    }

    @Test
    public void testAddingPeerToPrivateChannelWithPeerAsNull() {
        String actorCommonName = "actor";
        AddPeerRequest newPeer = new AddPeerRequest(null);
        assertThrows(PrivateChannelException.class, () -> napRestController.addPeerToPrivateChannel(actorCommonName, "validId", newPeer));
    }

    @Test
    public void testAddingPeerToPrivateChannelWithNonExistingId() {
        String actorCommonName = "actor";
        AddPeerRequest newPeer = new AddPeerRequest("newPeer");
        assertThrows(NotFoundException.class, () -> napRestController.addPeerToPrivateChannel(actorCommonName, "notAnId", newPeer));
    }

    @Test
    public void testDeletingPeerFromPrivateChannel() {
        String actorCommonName = "actor";
        PrivateChannelRequest request = new PrivateChannelRequest(new HashSet<>(Arrays.asList("peerOne", "peerTwo")), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);
        PrivateChannel savedChannel = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId());
        savedChannel.setStatus(no.vegvesen.ixn.federation.model.PrivateChannelStatus.CREATED);
        privateChannelRepository.save(savedChannel);

        napRestController.deletePeerFromPrivateChannel(actorCommonName, response.getId(), "peerTwo");
        Peer peerToTearDown = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId()).getPeers().stream().filter(p -> p.getStatus().equals(PeerStatus.TEAR_DOWN)).findFirst().get();
        assertThat(peerToTearDown.getName()).isEqualTo("peerTwo");
    }

    @Test
    public void testDeletingPeerFromPrivateChannelWithNonExistingId() {
        String actorCommonName = "actor";
        assertThrows(NotFoundException.class, () -> napRestController.deletePeerFromPrivateChannel(actorCommonName, "notAnId", "peer"));
    }

    @Test
    public void testDeletingPeerFromPrivateChannelWithNonExistingPeer() {
        String actorCommonName = "actor";
        PrivateChannelRequest request = new PrivateChannelRequest(new HashSet<>(Arrays.asList("peerOne", "peerTwo")), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);
        PrivateChannel savedChannel = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId());
        savedChannel.setStatus(no.vegvesen.ixn.federation.model.PrivateChannelStatus.CREATED);
        privateChannelRepository.save(savedChannel);

        assertThrows(NotFoundException.class, () -> napRestController.deletePeerFromPrivateChannel(actorCommonName, response.getId(), "nonExistingPeer"));
    }

    @Test
    public void testDeletingPeerFromPrivateChannelByPeer() {
        String actorCommonName = "actor";
        String peerToDelete = "peerTwo";
        PrivateChannelRequest request = new PrivateChannelRequest(new HashSet<>(Arrays.asList("peerOne", peerToDelete)), "My private channel");

        PrivateChannelResponse response = napRestController.addPrivateChannel(actorCommonName, request);
        PrivateChannel savedChannel = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId());
        savedChannel.setStatus(no.vegvesen.ixn.federation.model.PrivateChannelStatus.CREATED);
        privateChannelRepository.save(savedChannel);

        napRestController.peerDeletePeerFromPrivateChannel(peerToDelete, response.getId());
        Peer peerToTearDown = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, response.getId()).getPeers().stream().filter(p -> p.getStatus().equals(PeerStatus.TEAR_DOWN)).findFirst().get();
        assertThat(peerToTearDown.getName()).isEqualTo("peerTwo");
    }

    @Test
    public void testDeletingPeerFromPrivateChannelByPeerWithNonExistingId() {
        String peerToDelete = "peerToDelete";
        assertThrows(NotFoundException.class, () -> napRestController.peerDeletePeerFromPrivateChannel(peerToDelete, "notAnId"));
    }

    @Test
    public void testDeletingPeerFromPrivateChannelRemovesItImmediatelyFromListEndpoint(){
        String actorCommonName = "actor";
        String peerToDelete = "peerTwo";
        privateChannelRepository.save(new PrivateChannel(new HashSet<>(Set.of(new Peer(peerToDelete))), no.vegvesen.ixn.federation.model.PrivateChannelStatus.CREATED, "test",
                new no.vegvesen.ixn.federation.model.PrivateChannelEndpoint("test", 1337, "test"),
                actorCommonName));
        String privateChannelId = privateChannelRepository.findAllByServiceProviderName(actorCommonName).stream().findFirst().get().getUuid();
        napRestController.deletePeerFromPrivateChannel(actorCommonName, privateChannelId, peerToDelete);

        assertThat(napRestController.getPrivateChannels(actorCommonName).getFirst().getPeers()).hasSize(0);
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
