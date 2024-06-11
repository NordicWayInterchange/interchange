package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import jakarta.transaction.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class OnboardRestControllerIT {


    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private NeighbourRepository neighbourRepository;

    @Autowired
    private PrivateChannelRepository privateChannelRepository;

    @MockBean
    private CertService certService;

    @Autowired
    private InterchangeNodeProperties nodeProperties;

    @Autowired
    private OnboardRestController restController;

    @Test
    public void testAddCapabilitiesWithNullRequest() {
        assertThatExceptionOfType(CapabilityPostException.class).isThrownBy(
                () -> restController.addCapabilities("serviceProvider", null)
        );
    }

    @Test
    public void testAddCapabilitiesWithNullCapabilitySet() {
        AddCapabilitiesRequest request = new AddCapabilitiesRequest();
        request.setCapabilities(null);
        assertThatExceptionOfType(CapabilityPostException.class).isThrownBy(
                () -> restController.addCapabilities("serviceProvider", request)
        );
    }

    @Test
    public void testAddCapabilitiesWithEmptyCapabilitySet() {
        AddCapabilitiesRequest request = new AddCapabilitiesRequest("serviceProvider", new HashSet<>());
        assertThatExceptionOfType(CapabilityPostException.class).isThrownBy(
                () -> restController.addCapabilities("serviceProvider", request)
        );
    }

    @Test
    public void testWrongNameInRequestResultsInError() {
        AddCapabilitiesRequest request = new AddCapabilitiesRequest(
                "serviceProvider",
                Collections.singleton(
                        new CapabilityApi(
                                new DenmApplicationApi(
                                        "Publisher1",
                                        "Publisher1:Publication1",
                                        "NO",
                                        "1.0",
                                        List.of(),
                                        List.of()
                                ),
                                new MetadataApi()
                        )
                )
        );
        assertThatExceptionOfType(CapabilityPostException.class).isThrownBy(
                () -> restController.addCapabilities("anotherServiceProvider", request)
        );
    }

    @Test
    public void testAddingCapabilityWithPublisherIdMatchingALocalCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO00000", "NO-pub-1", "NO", "1.0", List.of("1200"), "SituationPublication", "publisherName");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilityApi datexNO = new CapabilityApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);

        ServiceProvider other = new ServiceProvider("other-service-provider");
        other.setCapabilities(new Capabilities(
                Collections.singleton(
                        new Capability(
                                new DatexApplication(
                                        "NO00000",
                                        "NO-pub-1",
                                        "NO",
                                        "1.0",
                                        List.of("1200"),
                                        "SituationPublication",
                                        "publisherName"),
                                new Metadata()
                        ))));

        serviceProviderRepository.save(other);

        String serviceProviderName = "my-service-provider";

        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName,
                new AddCapabilitiesRequest(
                        serviceProviderName,
                        Collections.singleton(datexNO)
                )));

        assertThat(thrown.getMessage()).contains("publicationId");
    }

    @Test
    public void testAddingCapabilityWithInvalidQuadTree(){
        DatexApplicationApi application = new DatexApplicationApi("pub-1-NOOOOOOO","NO-pub-1", "NO", "1.0", List.of("12004"), "SituationPublication", "publisherName");
        CapabilityApi datexNO = new CapabilityApi();
        datexNO.setApplication(application);

        String serviceProviderName = "my-service-provider";
        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName,
                new AddCapabilitiesRequest(
                        serviceProviderName,
                        Collections.singleton(datexNO)
                )));
        System.out.println(thrown.getMessage());
        assertThat(thrown.getMessage()).contains("invalid quadTree");
    }

    @Test
    public void testAddingCapabilityWithMissingProperties() {
        DatexApplicationApi app = new DatexApplicationApi("", "NO-pub-1", "NO", "1.0", List.of("1200"), "SituationPublication", "publisherName");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilityApi datexNO = new CapabilityApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);

        String serviceProviderName = "my-service-provider";
        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName,
                new AddCapabilitiesRequest(
                        serviceProviderName,
                        Collections.singleton(datexNO)
                )));

        assertThat(thrown.getMessage()).contains("publisherId");
    }

    @Test
    public void testAddingMultipleCapabilitiesWithOneInvalidCapabilityReturnsWithoutSavingAny(){
        String serviceProviderName = "serviceProviderMissingProperties";
        ServiceProvider sp = new ServiceProvider(serviceProviderName);
        Set<CapabilityApi> capabilities = new HashSet<>();
        serviceProviderRepository.save(sp);
        for(int i = 0; i<10; i++){
            capabilities.add(new CapabilityApi(
                    new DatexApplicationApi("pub1", "NO-pub-"+i, "NO","DATEX2:2.2",List.of("1230123"), "SituationPublication", "pubname"),
                    new MetadataApi()));
        }
        capabilities.add(new CapabilityApi(
                new DatexApplicationApi("pub1", "NO-pub-99999", "NO","DATEX2:2.2", List.of("1230123"), "SituationPublication", ""),
                new MetadataApi()
        ));
        assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName, new AddCapabilitiesRequest(serviceProviderName,capabilities)));

        assertThat(serviceProviderRepository.findByName(serviceProviderName).getCapabilities().getCapabilities().size()).isEqualTo(0);
    }

    @Test
    public void testListCapabilities() {
        String serviceProviderName = "serviceprovider";

        DatexApplicationApi app = new DatexApplicationApi("NO00000", "NO-pub-1", "NO", "1.0", List.of("1200"), "SituationPublication", "publisherName");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilityApi datexNO = new CapabilityApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);

        AddCapabilitiesRequest request = new AddCapabilitiesRequest(serviceProviderName, Set.of(datexNO));
        restController.addCapabilities(serviceProviderName, request);
        assertThat(restController.listCapabilities(serviceProviderName).getCapabilities().size()).isEqualTo(1);

    }

    @Test
    public void testAddingCapabilityWithPublisherIdMatchingANeighbourCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO00000", "NO-pub-1", "NO", "1.0", List.of("1200"), "SituationPublication", "publisherName");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilityApi datexNO = new CapabilityApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);

        Neighbour other = new Neighbour("my-neighbour",
                new NeighbourCapabilities(CapabilitiesStatus.KNOWN,
                        Collections.singleton(
                                new NeighbourCapability(
                                        new DatexApplication(
                                                "NO00000",
                                                "NO-pub-1",
                                                "NO",
                                                "1.0",
                                                List.of("1200"),
                                                "SituationPublication",
                                                "publisherName"),
                                        new Metadata()
                                ))),
                new NeighbourSubscriptionRequest(),
                new SubscriptionRequest()
        );

        neighbourRepository.save(other);

        String serviceProviderName = "my-service-provider";

        CapabilityPostException thrown = assertThrows(CapabilityPostException.class, () -> restController.addCapabilities(serviceProviderName,
                new AddCapabilitiesRequest(
                        serviceProviderName,
                        Collections.singleton(datexNO)
                )));

        assertThat(thrown.getMessage()).contains("publicationId");
    }

    @Test
    void testFetchingAllMatchingCapabilities() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.setCapabilities(new Capabilities(
                Collections.singleton(new Capability(
                        new DenmApplication(
                                "NPRA",
                                "pub-1",
                                "NO",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(serviceProvider);
        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Collections.singleton(new Capability(
                        new DenmApplication(
                                "SPRA",
                                "pub-2",
                                "SE",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new NeighbourCapabilities(
                CapabilitiesStatus.KNOWN,
                Collections.singleton(new NeighbourCapability(
                        new DenmApplication(
                                "DPRA",
                                "pub-3",
                                "DK",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        String selector = "messageType = 'DENM' and quadTree like '%,1234%' AND originatingCountry = 'NO'";

        FetchMatchingCapabilitiesResponse response = restController.listMatchingCapabilities(serviceProvider.getName(), selector);
        assertThat(response.getCapabilities()).hasSize(1);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        verify(certService, times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllCapabilitiesWhenServiceProviderExists() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.setCapabilities(new Capabilities(
                Collections.singleton(new Capability(
                        new DenmApplication(
                                "NPRA",
                                "pub-1",
                                "NO",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(serviceProvider);
        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Collections.singleton(new Capability(
                        new DenmApplication(
                                "SPRA",
                                "pub-2",
                                "SE",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new NeighbourCapabilities(
                CapabilitiesStatus.KNOWN,
                Collections.singleton(new NeighbourCapability(
                        new DenmApplication(
                                "DPRA",
                                "pub-3",
                                "DK",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchMatchingCapabilitiesResponse response = restController.listMatchingCapabilities(serviceProvider.getName(), null);
        assertThat(response.getCapabilities()).hasSize(3);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        verify(certService, times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllCapabilitiesWhenServiceProviderDoesNotExist() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");

        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Collections.singleton(new Capability(
                        new DenmApplication(
                                "SPRA",
                                "pub-1",
                                "SE",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new NeighbourCapabilities(
                CapabilitiesStatus.KNOWN,
                Collections.singleton(new NeighbourCapability(
                        new DenmApplication(
                                "DPRA",
                                "pub-2",
                                "DK",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchMatchingCapabilitiesResponse response = restController.listMatchingCapabilities(serviceProvider.getName(), "");
        assertThat(response.getCapabilities()).hasSize(2);
        assertThat(serviceProviderRepository.findAll()).hasSize(1);
        verify(certService, times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testFetchingAllMatchingCapabilitiesWhenSelectorIsNull() {
        ServiceProvider serviceProvider = new ServiceProvider("service-provider");
        serviceProvider.setCapabilities(new Capabilities(
                Collections.singleton(new Capability(
                        new DenmApplication(
                                "NPRA",
                                "pub-1",
                                "NO",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(serviceProvider);
        ServiceProvider otherServiceProvider = new ServiceProvider();
        otherServiceProvider.setCapabilities(new Capabilities(
                Collections.singleton(new Capability(
                        new DenmApplication(
                                "SPRA",
                                "pub-2",
                                "SE",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        serviceProviderRepository.save(otherServiceProvider);

        Neighbour neighbour = new Neighbour();
        neighbour.setCapabilities(new NeighbourCapabilities(
                CapabilitiesStatus.KNOWN,
                Collections.singleton(new NeighbourCapability(
                        new DenmApplication(
                                "DPRA",
                                "pub-3",
                                "DK",
                                "1.0",
                                List.of("1234"),
                                List.of(6)),
                        new Metadata(RedirectStatus.OPTIONAL)
                ))));
        neighbourRepository.save(neighbour);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        assertThat(neighbourRepository.findAll()).hasSize(1);

        FetchMatchingCapabilitiesResponse response = restController.listMatchingCapabilities(serviceProvider.getName(), null);
        assertThat(response.getCapabilities()).hasSize(3);
        assertThat(serviceProviderRepository.findAll()).hasSize(2);
        verify(certService, times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testDeletingCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO-123", "NO-pub", "NO", "1.0", List.of("1200"), "SituationPublication", "publisherName");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilityApi datexNO = new CapabilityApi();
        datexNO.setApplication(app);
        datexNO.setMetadata(meta);
        String serviceProviderName = "serviceprovider";

        AddCapabilitiesResponse addedCapability = restController.addCapabilities(serviceProviderName, new AddCapabilitiesRequest(
                serviceProviderName,
                Collections.singleton(datexNO)
        ));
        assertThat(addedCapability).isNotNull();
        ListCapabilitiesResponse serviceProviderCapabilities = restController.listCapabilities(serviceProviderName);
        assertThat(serviceProviderCapabilities.getCapabilities()).hasSize(1);

        //Test that we don't mess up subscriptions and capabilities
        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(0);

        LocalActorCapability saved = serviceProviderCapabilities.getCapabilities().iterator().next();
        //LocalCapability saved = serviceProviderCapabilities.getCapabilities().iterator().next();
        restController.deleteCapability(serviceProviderName, saved.getId());

        //We did four calls to the controller, thus we should have checked the cert 4 times
        verify(certService, times(4)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testDeletingNonExistentCapability() {
        String serviceProviderName = "serviceprovider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deleteCapability(serviceProviderName, "1")
        );
    }

    @Test
    public void testDeletingCapabilityWithInvalidId() {
        String serviceProviderName = "serviceprovider";
        String invalidId = "notAnId";

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deleteCapability(serviceProviderName, invalidId)
        );
    }

    @Test
    public void testGettingCapability() {
        DatexApplicationApi app = new DatexApplicationApi("NO-123", "NO-pub", "NO", "1.0", List.of("1200"), "SituationPublication", "publisherName");
        MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
        CapabilityApi datexNO = new CapabilityApi(
                app,
                meta
        );
        String serviceProviderName = "serviceprovider";

        AddCapabilitiesResponse addedCapability = restController.addCapabilities(serviceProviderName, new AddCapabilitiesRequest(
                serviceProviderName,
                Collections.singleton(datexNO)
        ));
        assertThat(addedCapability).isNotNull();
        Set<LocalActorCapability> capabilities = addedCapability.getCapabilities();
        assertThat(capabilities).hasSize(1);
        LocalActorCapability capability = capabilities.stream().findFirst()
                .orElseThrow(() -> new AssertionError("No capabilities in response"));
        GetCapabilityResponse response = restController.getCapability(serviceProviderName, capability.getId());
        assertThat(response.getId()).isEqualTo(capability.getId());

        verify(certService, times(2)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testGettingCapabilityWithInvalidId() {
        String serviceProviderName = "serviceprovider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.getCapability(serviceProviderName, "notAnId")
        );
    }

    @Test
    public void testGettingNonExistentCapability() {
        String serviceProviderName = "serviceprovider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deleteCapability(serviceProviderName, "1")
        );
    }

    @Test
    public void testAddingIllegalSubscription() {
        String serviceProviderName = "serviceprovider";
        String selector = "";

        AddSubscription addSubscription = new AddSubscription(selector, serviceProviderName);
        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));

        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProviderName, requestApi);

        LocalActorSubscription addedSubscription = response.getSubscriptions().stream()
                .findFirst()
                .get();

        assertThat(response.getSubscriptions()).hasSize(1);
        assertThat(addedSubscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.ERROR);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testAddSubscriptionNullArgument() {
        assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(
                () -> restController.addSubscriptions("serviceProvider", null)
        );
    }

    @Test
    public void testAddSubscriptionsNullSubscriptionSet() {
        AddSubscriptionsRequest request = new AddSubscriptionsRequest();
        request.setSubscriptions(null);
        assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(
                () -> restController.addSubscriptions("serviceProvider", request)
        );
    }

    @Test
    public void testAddSubscrptionsEmptySubscriptionsSet() {
        AddSubscriptionsRequest request = new AddSubscriptionsRequest("serviceProvider", Collections.emptySet());
        assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(
                () -> restController.addSubscriptions("serviceProvider", request)
        );

    }

    @Test
    public void testAddingSubscriptionConsumerCommonNameAsIxnName() {
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        String serviceProvider = "serviceprovider";
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(serviceProvider, Collections.singleton(new AddSubscription(selector, nodeProperties.getName())));
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProvider, request);
        assertThat(response.getSubscriptions()).hasSize(1);
        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.REQUESTED);
        verify(certService, times(1)).checkIfCommonNameMatchesNameInApiObject(serviceProvider);
    }

    @Test
    public void testAddingSubscriptionWithEmptyConsumerCommonName() {
        String serviceProviderName = "serviceprovider";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscription addSubscription = new AddSubscription(selector);

        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProviderName, requestApi);

        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.REQUESTED);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testAddingSubscriptionWithWrongConsumerCommonName() {
        String serviceProviderName = "serviceprovider";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscription addSubscription = new AddSubscription(selector, "anna");

        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProviderName, requestApi);

        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.ERROR);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testAddingSubscriptionWhenAnIdenticalWithDifferentStatusAlreadyExists() {
        String selector = "messageType = 'DATEX2' and originatingCountry = 'NO'";
        String serviceproviderName = "serviceprovider";
        ServiceProvider serviceProvider = new ServiceProvider(
                serviceproviderName,
                new Capabilities(Collections.emptySet()),
                Collections.singleton(new LocalSubscription(LocalSubscriptionStatus.ILLEGAL, selector, nodeProperties.getName())),
                Collections.emptySet(),
                LocalDateTime.now()
        );
        serviceProviderRepository.save(serviceProvider);

        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                serviceproviderName,
                Collections.singleton(new AddSubscription(selector))
        );
        AddSubscriptionsResponse response = restController.addSubscriptions(serviceproviderName, request);
        assertThat(response.getSubscriptions()).hasSize(1);
        LocalActorSubscription subscription = response.getSubscriptions().stream().findFirst().get();
        assertThat(subscription.getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.ILLEGAL); //the original one should be the one there
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceproviderName);
    }

    @Test
    public void testAddingInvalidSubscriptionObject() {
        String serviceProviderName = "serviceprovider";
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(serviceProviderName,
                null);

        SubscriptionRequestException thrown = assertThrows(SubscriptionRequestException.class, () -> {
            restController.addSubscriptions(serviceProviderName, request);
        });
        assertThat(thrown.getMessage()).isEqualTo("Bad api object for Subscription Request. No selectors.");
    }

    @Test
    public void testAddingSubscriptionWithInvalidSelector() {
        String selector = "Invalid selector";
        String serviceProviderName = "serviceprovider";
        ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

        serviceProviderRepository.save(serviceProvider);
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                serviceProviderName,
                Collections.singleton(new AddSubscription(selector))
        );

        AddSubscriptionsResponse response = restController.addSubscriptions(serviceProviderName, request);

        assertThat(response.getSubscriptions()).hasSize(1);
        assertThat(response.getSubscriptions().stream().findFirst().get().getErrorMessage()).isNotBlank();
        assertThat(response.getSubscriptions().stream().findFirst().get().getStatus()).isEqualTo(LocalActorSubscriptionStatusApi.ERROR);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    void testAddingLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderName() {
        String serviceProviderName = "service-provider-create-new-queue";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(new AddSubscription(selector, serviceProviderName))));

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

        ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalSubscription> localSubscriptions = savedSP.getSubscriptions();
        assertThat(localSubscriptions).hasSize(1);
        LocalSubscription subscription = localSubscriptions.stream().findFirst().get();

        assertThat(subscription.getConsumerCommonName()).isEqualTo(serviceProviderName);

        ListSubscriptionsResponse subscriptions = restController.listSubscriptions(serviceProviderName);
        Set<LocalActorSubscription> localSubscriptionApis = subscriptions.getSubscriptions();
        assertThat(localSubscriptionApis.size()).isEqualTo(1);
        verify(certService, times(3)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    void testAddingLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderNameAndGetApiObject() {
        String serviceProviderName = "service-provider-create-new-queue";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscriptionsResponse serviceProviderSubscriptions = restController.addSubscriptions(serviceProviderName, new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(new AddSubscription(selector, serviceProviderName))));
        verify(certService, times(1)).checkIfCommonNameMatchesNameInApiObject(anyString());
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);
    }

    @Test
    public void testDeletingSubscription() {
        LocalDateTime beforeDeleteTime = LocalDateTime.now();
        String serviceProviderName = "serviceprovider";
        String selector = "messageType = 'DATEX2' AND originatingCountry = 'NO'";
        AddSubscription addSubscription = new AddSubscription(selector, serviceProviderName);

        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(serviceProviderName, Collections.singleton(addSubscription));
        restController.addSubscriptions(serviceProviderName, requestApi);

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);

        ServiceProvider afterAddSubscription = serviceProviderRepository.findByName(serviceProviderName);
        assertThat(afterAddSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));

        LocalActorSubscription subscriptionApi = serviceProviderSubscriptions.getSubscriptions().stream().findFirst().get();
        restController.deleteSubscription(serviceProviderName, subscriptionApi.getId());

        ServiceProvider afterDeletedSubscription = serviceProviderRepository.findByName(serviceProviderName);
        assertThat(afterDeletedSubscription.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(beforeDeleteTime));
        verify(certService, times(3)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testDeletingSubscriptionWithInvalidId() {
        String serviceProviderName = "serviceprovider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deleteSubscription(serviceProviderName, "notAnId")
        );
        verify(certService, times(1)).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testDeletingNonExistentSubscription() {
        String serviceprovidername = "serviceprovider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deleteSubscription(serviceprovidername, "1")
        );
    }

    @Test
    void testDeletingNonExistingSubscriptionDoesNotModifyLastUpdatedSubscription() {
        String serviceProviderName = "serviceprovider-non-existing-subscription-delete";
        AddSubscriptionsRequest requestApi = new AddSubscriptionsRequest(
                serviceProviderName,
                Collections.singleton(new AddSubscription("messageType = 'DATEX2' AND originatingCountry = 'NO'", "my-node"))
        );
        restController.addSubscriptions(serviceProviderName, requestApi);

        ListSubscriptionsResponse serviceProviderSubscriptions = restController.listSubscriptions(serviceProviderName);
        assertThat(serviceProviderSubscriptions.getSubscriptions()).hasSize(1);
        ServiceProvider savedSP = serviceProviderRepository.findByName(serviceProviderName);
        Optional<LocalDateTime> subscriptionUpdated = savedSP.getSubscriptionUpdated();
        assertThat(subscriptionUpdated).isPresent();

        try {
            restController.deleteSubscription(serviceProviderName, "-1");
        } catch (NotFoundException ignore) {
        }
        ServiceProvider savedSPAfterDelete = serviceProviderRepository.findByName(serviceProviderName);

        assertThat(savedSPAfterDelete.getSubscriptionUpdated()).isEqualTo(subscriptionUpdated);
        verify(certService, times(3)).checkIfCommonNameMatchesNameInApiObject(anyString());
    }

    @Test
    public void testGettingSubscriptionWithInvalidId() {
        String serviceProviderName = "serviceprovider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.getSubscription(serviceProviderName, "notAnId")
        );
    }

    @Test
    public void testGettingNonExistentSubscription() {
        String serviceProviderName = "serviceprovider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.getSubscription(serviceProviderName, "1")
        );
    }

    @Test
    public void testGetSingleSubscription() {
        Set<AddSubscription> addSubscriptions = new HashSet<>();
        addSubscriptions.add(new AddSubscription("countryCode = 'SE' and messageType = 'DENM'", "king_olav.bouvetinterchange.eu"));
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                "king_olav.bouvetinterchange.eu",
                addSubscriptions
        );
        AddSubscriptionsResponse response = restController.addSubscriptions("king_olav.bouvetinterchange.eu", request);

        Optional<LocalActorSubscription> anySubscription = response.getSubscriptions().stream().findAny();
        LocalActorSubscription subscription = anySubscription.get();
        GetSubscriptionResponse getSubscriptionResponse = restController.getSubscription("king_olav.bouvetinterchange.eu", subscription.getId());
        assertThat(getSubscriptionResponse).isNotNull();
        verify(certService, times(2)).checkIfCommonNameMatchesNameInApiObject(anyString());
        assertThat(subscription.getConsumerCommonName()).isEqualTo("king_olav.bouvetinterchange.eu");
    }

    @Test
    public void testAddingChannels() {
        String serviceProviderName = "my-service-provider";

        PrivateChannelRequestApi clientChannel_1 = new PrivateChannelRequestApi("my-channel");
        PrivateChannelRequestApi clientChannel_2 = new PrivateChannelRequestApi("my-channel2");
        PrivateChannelRequestApi clientChannel_3 = new PrivateChannelRequestApi("my-channel3");

        restController.addPrivateChannels(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel_1, clientChannel_2, clientChannel_3)));

        assertThat(privateChannelRepository.findAll()).hasSize(3);
        PrivateChannelException thrown = assertThrows(PrivateChannelException.class, () -> restController.addPrivateChannels(serviceProviderName, null));
        assertThat(thrown.getMessage()).isEqualTo("Private channel can not be null");
    }

    @Test
    public void testAddingChannelWithServiceProviderAsPeerName() {
        String serviceProviderName = "my-service-provider";
        PrivateChannelRequestApi clientChannel = new PrivateChannelRequestApi(serviceProviderName);

        PrivateChannelException thrown = assertThrows(PrivateChannelException.class, () -> restController.addPrivateChannels(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel))));
        assertThat(thrown.getMessage()).isEqualTo("Can't add private channel with serviceProviderName as peerName");
    }

    @Test
    public void testAddingNullChannelsRequest() {
        AddPrivateChannelRequest request = null;
        assertThatExceptionOfType(PrivateChannelException.class).isThrownBy(
                () -> restController.addPrivateChannels("serviceProvider", request)
        );
    }

    @Test
    public void testAddingNullChannelSet() {
        AddPrivateChannelRequest request = new AddPrivateChannelRequest();
        request.setPrivateChannels(null);
        assertThatExceptionOfType(PrivateChannelException.class).isThrownBy(
                () -> restController.addPrivateChannels("serviceProvider", request)
        );
    }

    @Test
    public void testAddingEmptyChannelsSet() {
        AddPrivateChannelRequest request = new AddPrivateChannelRequest();
        assertThatExceptionOfType(PrivateChannelException.class).isThrownBy(
                () -> restController.addPrivateChannels("serviceProvider", request)
        );
    }

    @Test
    public void testAddingAndDeletingChannel() {
        String serviceProviderName = "my-service-provider";
        PrivateChannelRequestApi clientChannel = new PrivateChannelRequestApi("my-channel");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest(List.of(clientChannel));

        AddPrivateChannelResponse response = restController.addPrivateChannels(serviceProviderName, request);

        System.out.println(response.getPrivateChannels().get(0));
        restController.deletePrivateChannel(serviceProviderName, response.getPrivateChannels().get(0).getId().toString());
        assertThat(privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.TEAR_DOWN, serviceProviderName).size()).isEqualTo(1);
    }

    @Test
    public void testDeletingNonExistentChannel() {
        String serviceProviderName = "my-service-provider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deletePrivateChannel(serviceProviderName, "1")
        );
    }

    @Test
    public void testDeletingInvalidChannelId() {
        String serviceProviderName = "my-service-provider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deletePrivateChannel(serviceProviderName, "notAnId")
        );
    }

    @Test
    public void testGettingPrivateChannels() {
        String serviceProviderName = "my-service-provider";
        PrivateChannelRequestApi clientChannel_1 = new PrivateChannelRequestApi("my-channel");
        PrivateChannelRequestApi clientChannel_2 = new PrivateChannelRequestApi("my-channel2");
        PrivateChannelRequestApi clientChannel_3 = new PrivateChannelRequestApi("my-channel3");
        restController.addPrivateChannels(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel_1, clientChannel_2, clientChannel_3)));
        ListPrivateChannelsResponse response = restController.listPrivateChannels(serviceProviderName);
        assertThat(response.getPrivateChannels().size()).isEqualTo(3);
    }

    @Test
    public void testGettingChannel() {
        String serviceProviderName = "my-service-provider";
        PrivateChannelRequestApi clientChannel_1 = new PrivateChannelRequestApi("my-channel");
        PrivateChannelRequestApi clientChannel_2 = new PrivateChannelRequestApi("my-channel2");
        PrivateChannelRequestApi clientChannel_3 = new PrivateChannelRequestApi("my-channel3");

        AddPrivateChannelResponse privateChannels = restController.addPrivateChannels(serviceProviderName, new AddPrivateChannelRequest(List.of(clientChannel_1, clientChannel_2, clientChannel_3)));
        GetPrivateChannelResponse channelResponse = restController.getPrivateChannel(serviceProviderName, privateChannels.getPrivateChannels().get(0).getId().toString());

        assertThat(channelResponse).isNotNull();
    }

    @Test
    public void testGettingNonExistentChannel() {
        String serviceProviderName = "my-service-provider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.getPrivateChannel(serviceProviderName, "1")
        );
    }

    @Test
    public void testGettingChannelWithInvalidId() {
        String serviceProviderName = "my-service-provider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.getPrivateChannel(serviceProviderName, "notAnId")
        );
    }

    @Test
    public void testGetPeerPrivateChannels() {
        String serviceProviderName_1 = "my-service-provider";
        String serviceProviderName_2 = "my-service-provider2";

        PrivateChannelRequestApi clientChannel_1 = new PrivateChannelRequestApi("my-channel");
        PrivateChannelRequestApi clientChannel_2 = new PrivateChannelRequestApi(serviceProviderName_1);

        restController.addPrivateChannels(serviceProviderName_1, new AddPrivateChannelRequest(List.of(clientChannel_1)));
        restController.addPrivateChannels(serviceProviderName_2, new AddPrivateChannelRequest(List.of(clientChannel_2)));

        ListPeerPrivateChannels response_1 = restController.listPeerPrivateChannels(serviceProviderName_1);
        ListPeerPrivateChannels response_2 = restController.listPeerPrivateChannels(serviceProviderName_2);
        assertThat(response_1.getPrivateChannels().size()).isEqualTo(1);
        assertThat(response_2.getPrivateChannels().size()).isEqualTo(0);
    }

    @Test
    public void testAddingNullRequest() {
        assertThatExceptionOfType(DeliveryPostException.class).isThrownBy(
                () -> restController.addDeliveries("serviceProvider", null)
        );
    }

    @Test
    public void testAddingNullDelivery() {
        AddDeliveriesRequest request = new AddDeliveriesRequest();
        request.setDeliveries(null);
        assertThatExceptionOfType(DeliveryPostException.class).isThrownBy(
                () -> restController.addDeliveries("serviceProvider", request)
        );
    }

    @Test
    public void testAddingEmptyDeliverySet() {
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                "serviceProvider",
                Collections.emptySet()
        );
        assertThatExceptionOfType(DeliveryPostException.class).isThrownBy(
                () -> restController.addDeliveries("serviceProvider", request)
        );
    }

    @Test
    public void testAddingIllegalDelivery() {
        String serviceProviderName = "my-service-provider";
        String selector = "";

        SelectorApi delivery = new SelectorApi(selector);
        AddDeliveriesRequest requestApi = new AddDeliveriesRequest(serviceProviderName, Collections.singleton(delivery));

        AddDeliveriesResponse response = restController.addDeliveries(serviceProviderName, requestApi);

        Delivery addedDelivery = response.getDeliveries().stream()
                .findFirst()
                .get();

        assertThat(response.getDeliveries()).hasSize(1);
        assertThat(addedDelivery.getStatus()).isEqualTo(DeliveryStatus.ERROR);
        verify(certService).checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
    }

    @Test
    public void testAddingMoreThanOneIdenticalDeliveries() {
        String serviceProviderName = "my-service-provider";
        String selector = "messageType='DENM'";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Collections.singleton(
                        new SelectorApi(selector)
                )
        );
        AddDeliveriesResponse response = restController.addDeliveries(serviceProviderName, request);
        assertThat(response.getDeliveries()).hasSize(1);
        assertThat(response.getDeliveries()).allMatch(d -> d.getStatus().equals(DeliveryStatus.REQUESTED));

        //change the delivery status in the database
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        assertThat(serviceProvider.getDeliveries()).hasSize(1);
        serviceProvider.getDeliveries().stream().forEach(d -> d.setStatus(LocalDeliveryStatus.CREATED));
        serviceProviderRepository.save(serviceProvider);

        //now, add the second delivery with original status
        response = restController.addDeliveries(serviceProviderName, request);
        assertThat(response.getDeliveries()).hasSize(1);

        serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        assertThat(serviceProvider.getDeliveries()).hasSize(1);
    }

    @Test
    public void testAddingDeliveryWithInvalidSelector() {

        String serviceProviderName = "my-service-provider";
        String selector = "Invalid selector";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Collections.singleton(
                        new SelectorApi(selector)
                )
        );

        serviceProviderRepository.save(new ServiceProvider(serviceProviderName));
        AddDeliveriesResponse response = restController.addDeliveries(serviceProviderName, request);
        assertThat(response.getDeliveries()).hasSize(1);

        Delivery delivery = response.getDeliveries().stream().findFirst().get();
        assertThat(delivery.getErrorMessage()).isNotBlank();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.ERROR);
    }

    @Test
    public void testAddingDeliveryWithNoSelector() {
        String serviceProviderName = "my-service-provider";
        String selector = null;

        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Collections.singleton(
                        new SelectorApi(selector)
                )
        );
        serviceProviderRepository.save(new ServiceProvider(serviceProviderName));
        AddDeliveriesResponse response = restController.addDeliveries(serviceProviderName, request);
        assertThat(response.getDeliveries()).hasSize(1);

        Delivery delivery = response.getDeliveries().stream().findFirst().get();
        assertThat(delivery.getErrorMessage()).isEqualTo("Bad api object for adding delivery. The selector object was null.");
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.ERROR);
    }

    @Test
    public void testListDeliveries() {
        String serviceProviderName = "my-service-provider";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Set.of(
                        new SelectorApi("messageType = 'DENM'")
                )
        );
        restController.addDeliveries(serviceProviderName, request);
        ListDeliveriesResponse response = restController.listDeliveries(serviceProviderName);

        assertThat(response.getDeliveries().size()).isEqualTo(1);
        verify(certService, times(2)).checkIfCommonNameMatchesNameInApiObject(anyString());

    }

    @Test
    public void testGettingDelivery() {
        String serviceProviderName = "my-service-provider";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Set.of(
                        new SelectorApi("messageType = 'DENM'")
                )
        );
        AddDeliveriesResponse addDeliveriesResponse = restController.addDeliveries(serviceProviderName, request);

        String deliveryId = addDeliveriesResponse.getDeliveries().stream().findFirst().get().getId();
        GetDeliveryResponse getDeliveryResponse = restController.getDelivery(serviceProviderName, deliveryId);

        assertThat(getDeliveryResponse).isNotNull();
        assertThrows(NotFoundException.class, () -> {
            restController.getDelivery(serviceProviderName, "999");
        });
    }

    @Test
    public void testGettingDeliveryWithInvalidId() {
        String serviceProviderName = "my-service-provider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.getDelivery(serviceProviderName, "notAnId")
        );
    }

    @Test
    public void testDeletingDelivery() {

        String serviceProviderName = "my-service-provider";
        String selector = "messageType='DENM'";
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                serviceProviderName,
                Collections.singleton(
                        new SelectorApi(selector)
                )
        );
        AddDeliveriesResponse response = restController.addDeliveries(serviceProviderName, request);

        restController.deleteDelivery(serviceProviderName, response.getDeliveries().stream().findFirst().get().getId());
        assertThat(restController.listDeliveries(serviceProviderName).getDeliveries()
                .stream()
                .filter(i -> i.getStatus().equals(DeliveryStatus.ILLEGAL)))
                .hasSize(1);
    }

    @Test
    public void testDeletingNonExistentDelivery() {
        String serviceProviderName = "my-service-provider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deleteDelivery(serviceProviderName, "1")
        );
    }

    @Test
    public void testDeletingDeliveryWithInvalidId() {
        String serviceProviderName = "my-service-provider";
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> restController.deleteDelivery(serviceProviderName, "notAnId")
        );
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
