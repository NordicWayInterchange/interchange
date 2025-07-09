package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NapRestController.class)
@ContextConfiguration(classes = {NapRestController.class, InterchangeNodeProperties.class, NapRestControllerTest.NapCorePropertiesCreator.class, CapabilityToCapabilityApiTransformer.class})
public class NapRestControllerTest {

    public static final String NODE_NAME = "interchangenode";

    public static final String NAP_USER_NAME = "napcn";

    private MockMvc mockMvc;

    @MockitoBean
    ServiceProviderRepository serviceProviderRepository;

    @MockitoBean
    private NeighbourRepository neighbourRepository;

    @MockitoBean
    private PrivateChannelRepository privateChannelRepository;

    private NapCoreProperties napCoreProperties;

    @MockitoBean
    private CertSigner certSigner;

    @MockitoBean
    private CertService certService;

    @Autowired
    private NapRestController restController;

    @Autowired
    private CapabilityToCapabilityApiTransformer transformer;

    @Autowired
    private CapabilityToCapabilityApiTransformer capabilityToCapabilityApiTransformer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(restController)
                .setMessageConverters(NapStrictWebConfig.strictJsonMessageConverter())
                .setControllerAdvice(NapServerErrorAdvice.class)
                .alwaysExpect(content().contentType(MediaType.APPLICATION_JSON))
                .build();
    }

    @Test
    @DisplayName("Get existing local subscription")
    public void getLocalSubscriptions() throws Exception {
        String serviceProviderName = "sp-1";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(
                        new LocalSubscription(
                                1,
                                LocalSubscriptionStatus.REQUESTED,
                                "originatingCountry = 'NO'",
                                serviceProviderName,
                                Collections.emptySet(),
                                Collections.singleton(
                                        new LocalEndpoint(
                                                "my-source",
                                                "my-host",
                                                5671,
                                                0,
                                                0
                                        )
                                )
                        )
                ),
                null
        );
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProvider.getName())).thenReturn(serviceProvider);

        mockMvc.perform(
                get(String.format("/nap/%s/subscriptions",serviceProviderName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*",isA(ArrayList.class)))
                .andExpect(jsonPath("$.*",hasSize(1)));
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository,times(1)).findByName(serviceProviderName);
    }

    @Test
    @DisplayName("Getting a non-existing Service Provider returns empty list")
    public void getNonexistingServiceProvider() throws Exception {
        String serviceProviderName = "sp-1";
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProviderName)).thenReturn(null);

        mockMvc.perform(
                        get(String.format("/nap/%s/subscriptions",serviceProviderName))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*",isA(ArrayList.class)))
                .andExpect(jsonPath("$.*",hasSize(0)));
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository,times(1)).findByName(serviceProviderName);
    }

    @Test
    @DisplayName("Get the details of a local subscription")
    public void getSingleLocalSubscription() throws Exception {
        String serviceProviderName = "sp-1";
        LocalSubscription localSubscription = new LocalSubscription(
                1,
                LocalSubscriptionStatus.REQUESTED,
                "originatingCountry = 'NO'",
                serviceProviderName,
                Collections.emptySet(),
                Collections.singleton(
                        new LocalEndpoint(
                                "my-source",
                                "my-host",
                                5671,
                                0,
                                0
                        )
                )
        );
        localSubscription.setUuid(UUID.randomUUID().toString());

        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.singleton(
                    localSubscription
                ),
                null
        );
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProviderName)).thenReturn(serviceProvider);
        mockMvc.perform(
                        get(String.format("/nap/%s/subscriptions/%s",serviceProviderName, localSubscription.getUuid()))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",is(localSubscription.getUuid())));
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository).findByName(serviceProviderName);
    }

    @Test
    @DisplayName("Get single, non-existing local subscription")
    public void getNonExistingSubscription() throws Exception {
        String serviceProviderName = "sp-1";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.emptySet(),
                null
        );
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(serviceProviderRepository.findByName(serviceProviderName)).thenReturn(serviceProvider);
        mockMvc.perform(
                        get(String.format("/nap/%s/subscriptions/%d",serviceProviderName,serviceProvider.getId()))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        verify(serviceProviderRepository).findByName(serviceProviderName);
    }

    @Test
    public void postingNapSubscriptionWithExtraFieldsReturnsStatusBadRequest() throws Exception {
        String request = """
                {
                "selector": "originatingCountry='NO'",
                "extra": "extra"
                }
                """;
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/subscriptions", "actor"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void postingInvalidNapSubscriptionRequestReturnsStatusBadRequest() throws Exception {
        String invalidRequest = "";
        String serviceProviderName = "actor";
        ServiceProvider serviceProvider = new ServiceProvider(
                1,
                serviceProviderName,
                new Capabilities(),
                Collections.emptySet(),
                null
        );
        when(serviceProviderRepository.findByName(any())).thenReturn(serviceProvider);
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/subscriptions", "actor"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void postDeliveryReturnsStatusOk() throws Exception{
        String request = """
                {
                "selector": "originatingCountry='NO'",
                "description": "NO delivery"
                }
                """;
        String serviceProviderName = "actor";
        ServiceProvider serviceProvider = new ServiceProvider(
                serviceProviderName,
                new Capabilities(),
                Set.of(),
                Set.of(),
                null
        );
        when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
        when(serviceProviderRepository.findByName(any())).thenReturn(serviceProvider);
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/deliveries", serviceProviderName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk());
    }

    @Test
    public void postDeliveryWithExtraFieldsReturnsStatusBadRequest() throws Exception{
        String request = """
                {
                "selector": "originatingCountry='NO'",
                "extraField": "extraField"
                """;

        String serviceProviderName = "actor";
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/deliveries", serviceProviderName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void postDeliveryWithNullSelectorReturnsStatusBadrequest() throws Exception{
        String request = "{}";
        String serviceProviderName = "actor";
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/deliveries", serviceProviderName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void deletingNonExistingNapSubscriptionReturnsStatusNotFound() throws Exception{
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                delete(String.format("/nap/%s/subscriptions/%s", "actor", "1"))
        ).andExpect(status().isNotFound());
    }

    @Test
    public void deletingNapSubscriptionWithInvalidIdReturnsStatusNotFound() throws Exception{
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                delete(String.format("/nap/%s/subscriptions/%s", "actor", "notAnId"))
        ).andExpect(status().isNotFound());
    }

    @Test
    public void addingCapabilityReturnsStatusOk() throws Exception{
        String request = """
                {
                "application":
                {
                "messageType": "DATEX2",
                "publisherId": "NO12345",
                "publicationId": "NO12345:publicationId",
                "protocolVersion": "protocolVersion",
                "quadTree": ["123"],
                "publicationType": "Hello",
                "publisherName": "hello",
                "originatingCountry": "NO"
                },
                "metadata": {}
                }
                """;

        String actorCommonName = "actor";
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        Capability capability = new Capability(
                1,
                new DatexApplication("NO12345", "NO12345:publicationId", "NO", "protocolVersion", List.of("123"), "Hello", "hello"),
                new Metadata()
        );
        when(serviceProviderRepository.save(any())).thenReturn(new ServiceProvider(
                1,
                actorCommonName,
                new Capabilities(Set.of(capability)),
                Set.of(),
                null
        ));
        mockMvc.perform(
                post(String.format("/nap/%s/capabilities", actorCommonName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk());
    }

    @Test
    public void addingCapabilityWithMissingFieldsReturnsStatusBadRequest() throws Exception{
        String request = """
                {
                "application":
                {
                "messageType": "DATEX2",
                "publisherId": "publisherId",
                "publicationId": "publisherId:publicationId",
                "quadTree": ["123"],
                "publicationType": "Hello",
                "publisherName": "hello",
                "originatingCountry": "NO"
                },
                "metadata": {}
                }
                """;
        String actorCommonName = "actor";
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        mockMvc.perform(
                post(String.format("/nap/%s/capabilities", actorCommonName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void addingPrivateChannelReturnsStatusOk() throws Exception {
        String actorCommonName = "king_olav.bouvetinterchange.eu";

        String request = """
                {
                "peers": ["king_gustaf.bouvetinterchange.eu", "king_bjarne.bouvetinterchange.eu"],
                "description": "Private channel for bouvet service providers"
                }
                """;
        PrivateChannel privateChannel = new PrivateChannel(
                Set.of(new Peer("king_gustaf.bouvetinterchange.eu"), new Peer("king_bjarne.bouvetinterchange.eu")), PrivateChannelStatus.REQUESTED, "private channel",
                new PrivateChannelEndpoint("test", 1337, "test"), actorCommonName);
        privateChannel.setLastUpdated(LocalDateTime.now());
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(privateChannelRepository.save(any())).thenReturn(privateChannel);
        mockMvc.perform(
                post(String.format("/nap/%s/privatechannels", actorCommonName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk());
    }

    @Test
    public void addingPrivateChannelWithActorCommonNameAsPeerReturnsStatusBadRequest() throws Exception {
        String actorCommonName = "king_gustaf.bouvetinterchange.eu";

        String request = """
                {
                "peers": ["king_gustaf.bouvetinterchange.eu", "king_bjarne.bouvetinterchange.eu"],
                "description": "Private channel for bouvet service providers"
                }
                """;
        PrivateChannel privateChannel = new PrivateChannel(
                Set.of(new Peer("king_gustaf.bouvetinterchange.eu"), new Peer("king_bjarne.bouvetinterchange.eu")), PrivateChannelStatus.REQUESTED, "private channel",
                new PrivateChannelEndpoint("test", 1337, "test"), actorCommonName);
        privateChannel.setLastUpdated(LocalDateTime.now());
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(privateChannelRepository.save(any())).thenReturn(privateChannel);
        mockMvc.perform(
                post(String.format("/nap/%s/privatechannels", actorCommonName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }

    @Test
    public void addingPrivateChannelWithEmptySetOfPeersReturnsStatusOk()throws Exception{
        String actorCommonName = "king_olav.bouvetinterchange.eu";

        String request = """
                {
                "peers": [],
                "description": "Private channel for bouvet service providers"
                }
                """;
        PrivateChannel privateChannel = new PrivateChannel(
                Set.of(), PrivateChannelStatus.REQUESTED, "king_olav.bouvetinterchange.eu",
                new PrivateChannelEndpoint("test", 1337, "test"), actorCommonName);
        privateChannel.setLastUpdated(LocalDateTime.now());
        doNothing().when(certService).checkIfCommonNameMatchesNapName(NAP_USER_NAME);
        when(privateChannelRepository.save(any())).thenReturn(privateChannel);
        mockMvc.perform(
                post(String.format("/nap/%s/privatechannels", actorCommonName))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk());
    }

    @Test
    public void getPrivateChannelReturnsStatusOk() throws Exception{
        String actorCommonName = "king_olav.bouvetinterchange.eu";
        PrivateChannel privateChannel = new PrivateChannel(
                Set.of(new Peer("king_gustaf.bouvetinterchange.eu"), new Peer("king_bjarne.bouvetinterchange.eu")), PrivateChannelStatus.REQUESTED, "private channel",
                new PrivateChannelEndpoint("test", 1337, "test"), actorCommonName);
        privateChannel.setUuid(UUID.randomUUID().toString());
        privateChannel.setLastUpdated(LocalDateTime.now());
        when(privateChannelRepository.findByServiceProviderNameAndUuid(any(), any())).thenReturn(privateChannel);

        mockMvc.perform(
          get(String.format("/nap/%s/privatechannels/%s", actorCommonName, privateChannel.getUuid()))
        ).andExpect(status().isOk());
    }

    @Test
    public void getNonExistentPrivateChannelReturnsStatusNotFound() throws Exception{
        String actorCommonName = "king_olav.bouvetinterchange.eu";
        mockMvc.perform(
                get(String.format("/nap/%s/privatechannels/%s", actorCommonName, UUID.randomUUID()))
        ).andExpect(status().isNotFound());
    }

    @Test
    public void getPrivateChannelsReturnsStatusOk() throws Exception{
        String actorCommonName = "king_olav.bouvetinterchange.eu";
        mockMvc.perform(
                get(String.format("/nap/%s/privatechannels", actorCommonName))
        ).andExpect(status().isOk());
    }

    @Test
    public void getPeerPrivateChannelsReturnsStatusOk() throws Exception{
        String actorCommonName = "king_olav.bouvetinterchange.eu";
        mockMvc.perform(
                get(String.format("/nap/%s/privatechannels/peer", actorCommonName))
        ).andExpect(status().isOk());
    }

    @Test
    public void getPeerPrivateChannelReturnsStatusOk() throws Exception{
        String actorCommonName = "king_olav.bouvetinterchange.eu";
        PrivateChannel privateChannel = new PrivateChannel(
                Set.of(new Peer("king_gustaf.bouvetinterchange.eu"), new Peer("king_bjarne.bouvetinterchange.eu")), PrivateChannelStatus.REQUESTED, "private channel",
                new PrivateChannelEndpoint("test", 1337, "test"), actorCommonName);
        privateChannel.setUuid(UUID.randomUUID().toString());
        privateChannel.setLastUpdated(LocalDateTime.now());
        when(privateChannelRepository.findByUuidAndPeerName(any(), any())).thenReturn(privateChannel);
        mockMvc.perform(
                get(String.format("/nap/%s/privatechannels/peer/%s", actorCommonName, privateChannel.getUuid()))
        ).andExpect(status().isOk());
    }

    @Test
    public void getNonExistentPeerPrivateChannelReturnsStatusNotFound() throws Exception{
        String actorCommonName = "king_olav.bouvetinterchange.eu";
        mockMvc.perform(
                get(String.format("/nap/%s/privatechannels/peer/%s", actorCommonName, UUID.randomUUID()))
        ).andExpect(status().isNotFound());
    }

    @Configuration
    public static class NapCorePropertiesCreator {
        @Bean
        public NapCoreProperties napCoreProperties() {
            return new NapCoreProperties(
                    NODE_NAME,
                    NAP_USER_NAME
            );
        }
    }
}
