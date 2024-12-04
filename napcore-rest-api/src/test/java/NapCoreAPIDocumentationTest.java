import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.napcore.model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class NapCoreAPIDocumentationTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCertificateResponse() throws IOException {
        CertificateSignResponse response = new ObjectMapper().readValue(Paths.get("src", "test", "resources", "certChainResponse.json").toFile(), CertificateSignResponse.class);
        assertThat(response.getChain()).hasSize(3);

        List<String> decoded = response.getChain().stream().map(s -> new String(Base64.getDecoder().decode(s))).collect(Collectors.toList());
        assertThat(decoded).allMatch(s -> s.startsWith("-----BEGIN CERTIFICATE-----\n")).allMatch(s -> s.endsWith("-----END CERTIFICATE-----\n"));

    }

    @Test
    public void addNapSubscriptionRequestTest() throws JsonProcessingException {
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                "originatingCountry = 'SE' and messageType = 'DENM' and quadTree like '%,12003%'", "DENM Sub");

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptionRequest));
    }

    @Test
    public void addNapSubscriptionResponseTest() throws JsonProcessingException {
        SubscriptionEndpoint endpoint = new SubscriptionEndpoint(
                "my-host",
                5671,
                "my-source",
                0,
                0
        );

        Subscription subscription = new Subscription(
                UUID.randomUUID().toString(),
                SubscriptionStatus.CREATED,
                "messageType = 'DENM'",
                new HashSet<>(Collections.singleton(endpoint)),
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond(),
                ""
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }

    @Test
    public void napSubscriptionCapabilityResponse() throws JsonProcessingException {
        Capability capability = new Capability(
                new DenmApplicationApi(
                        "ID0001,",
                        "ID0001:0001",
                        "NO",
                        "DENM:001",
                        List.of("123123"),
                        List.of(1, 2, 3)
                ),
                new MetadataApi()
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Arrays.asList(capability)));
    }

    @Test
    public void getSubscriptionsResponse() throws JsonProcessingException {
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(new Subscription(UUID.randomUUID().toString(), SubscriptionStatus.CREATED, "originatingCountry='NO'", Set.of(new SubscriptionEndpoint(
                "a.bouvetinterchange.eu",
                1337,
                "serviceProvider",
                1,
                5)), 94882124L, "NO subscription"));
        subscriptions.add(new Subscription(UUID.randomUUID().toString(), SubscriptionStatus.CREATED, "messageType='DATEX'", Set.of(new SubscriptionEndpoint(
                "a.bouvetinterchange.eu",
                1338,
                "serviceProvider",
                1,
                5)), 91234124L, "DATEX subscription"));

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriptions));
    }

    @Test
    public void getSubscriptionResponse() throws JsonProcessingException {
        Subscription subscription = new Subscription(UUID.randomUUID().toString(), SubscriptionStatus.CREATED, "originatingCountry='NO'", Set.of(new SubscriptionEndpoint(
                "a.bouvetinterchange.eu",
                1337,
                "serviceProvider",
                1,
                5)), 94882124L, "NO subscription");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(subscription));
    }

    @Test
    public void addDeliveryRequest() throws JsonProcessingException {
        DeliveryRequest delivery = new DeliveryRequest("originatingCountry='NO'", "NO Delivery");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(delivery));
    }

    @Test
    public void addDeliveryResponse() throws JsonProcessingException {
        Delivery delivery = new Delivery(UUID.randomUUID().toString(), "originatingCountry='NO'", DeliveryStatus.REQUESTED, null, 93124429L, "NO delivery");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(delivery));
    }

    @Test
    public void getDeliveryResponse() throws JsonProcessingException {
        Delivery delivery = new Delivery(UUID.randomUUID().toString(), "originatingCountry='NO'", DeliveryStatus.REQUESTED, List.of(new DeliveryEndpoint(
                "a.bouvetinterchange.eu",
                1337,
                "serviceProvider",
                "originatingCountry='NO'",
                1,
                5
        )), 93124429L, "NO delivery");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(delivery));
    }

    @Test
    public void getDeliveriesResponse() throws JsonProcessingException {
        List<Delivery> deliveries = new ArrayList<>();
        deliveries.add(new Delivery(UUID.randomUUID().toString(), "originatingCountry='NO'", DeliveryStatus.REQUESTED, List.of(new DeliveryEndpoint(
                "a.bouvetinterchange.eu",
                1337,
                "serviceProvider",
                "originatingCountry='NO'",
                1,
                5
        )), 93124429L, "NO delivery"));
        deliveries.add(new Delivery(UUID.randomUUID().toString(), "messageType='DATEX'", DeliveryStatus.REQUESTED, List.of(new DeliveryEndpoint(
                "a.bouvetinterchange.eu",
                1337,
                "serviceProvider",
                "messageType='DATEX'",
                1,
                5
        )), 93124469L, "DATEX delivery"));
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deliveries));
    }

    @Test
    public void getDeliveryCapabilityResponse() throws JsonProcessingException {
        Capability capability = new Capability(
                new DenmApplicationApi(
                        "ID0001,",
                        "ID0001:0001",
                        "NO",
                        "DENM:001",
                        List.of("123123"),
                        List.of(1, 2, 3)
                ),
                new MetadataApi()
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Arrays.asList(capability)));
    }

    @Test
    public void addCapabilityRequest() throws JsonProcessingException {
        CapabilitiesRequest capability = new CapabilitiesRequest(
                new DatexApplicationApi(
                        "ID1",
                        "ID1:firstPublication",
                        "NO",
                        "DATEX2:1.2",
                        List.of("123"),
                        "roadWorks",
                        "ID1"
                ),
                new MetadataApi()
        );

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));
    }

    @Test
    public void addCapabilityResponse() throws JsonProcessingException {
        OnboardingCapability capability = new OnboardingCapability(
                UUID.randomUUID().toString(),
                new DatexApplicationApi(
                        "ID1",
                        "ID1:firstPublication",
                        "NO",
                        "DATEX2:1.2",
                        List.of("123"),
                        "roadWorks",
                        "ID1"
                ),
                new MetadataApi(),
                95323215L);

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));
    }

    @Test
    public void getCapabilitiesResponse() throws JsonProcessingException {
        List<OnboardingCapability> capabilities = new ArrayList<>();
        capabilities.add(new OnboardingCapability(
                UUID.randomUUID().toString(),
                new DatexApplicationApi(
                        "ID1",
                        "ID1:firstPublication",
                        "NO",
                        "DATEX2:1.2",
                        List.of("123"),
                        "roadWorks",
                        "ID1"
                ),
                new MetadataApi(),
                95323215L));
        capabilities.add(new OnboardingCapability(
                UUID.randomUUID().toString(),
                new DenmApplicationApi(
                        "ID1",
                        "ID1:firstPublication",
                        "NO",
                        "DATEX2:1.2",
                        List.of("123"),
                        List.of(2)
                ),
                new MetadataApi(),
                95323218L));
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilities));
    }

    @Test
    public void getCapabilityResponse() throws JsonProcessingException {
        OnboardingCapability capability = new OnboardingCapability(
                UUID.randomUUID().toString(),
                new DatexApplicationApi(
                        "ID1",
                        "ID1:firstPublication",
                        "NO",
                        "DATEX2:1.2",
                        List.of("123"),
                        "roadWorks",
                        "ID1"
                ),
                new MetadataApi(),
                95323215L);

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capability));
    }

    @Test
    public void getPublicationIdsResponse() throws JsonProcessingException {
        Set<String> publicationIds = Set.of("bouvet:1", "bouvet:2", "bouvet:3");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(publicationIds));
    }

    @Test
    public void addPrivateChannelRequest() throws JsonProcessingException {
        PrivateChannelRequest privateChannel = new PrivateChannelRequest(
                Set.of("king_gustaf.bouvetinterchange.eu", "king_olav.bouvetinterchange.eu"),
                "private channel for gustaf and olav"
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateChannel));
    }

    @Test
    public void addPrivateChanelResponse() throws JsonProcessingException {
        PrivateChannelResponse privateChannel = new PrivateChannelResponse(
                UUID.randomUUID().toString(),
                Set.of("king_gustaf.bouvetinterchange.eu", "king_olav.bouvetinterchange.eu"),
                PrivateChannelStatus.REQUESTED,
                "private channel for gustaf and olav",
                null,
                98521521L
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateChannel));
    }

    @Test
    public void getPrivateChannelsResponse() throws JsonProcessingException {
        List<PrivateChannelResponse> privateChannels = new ArrayList<>();
        privateChannels.add(new PrivateChannelResponse(
                UUID.randomUUID().toString(),
                Set.of("king_gustaf.bouvetinterchange.eu", "king_olav.bouvetinterchange.eu"),
                PrivateChannelStatus.CREATED,
                "private channel for gustaf and olav",
                new PrivateChannelEndpoint("a.bouvetinterchange.eu", 1337, "priv-" + UUID.randomUUID()),
                98521521L
        ));
        privateChannels.add(new PrivateChannelResponse(
                UUID.randomUUID().toString(),
                Set.of("bjarne.bouvetinterchange.eu", "king_olav.bouvetinterchange.eu"),
                PrivateChannelStatus.CREATED,
                "private channel for bjarne and olav",
                new PrivateChannelEndpoint("a.bouvetinterchange.eu", 1337, "priv-" + UUID.randomUUID()),
                98521525L
        ));
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateChannels));
    }

    @Test
    public void getPrivateChannelResponse() throws JsonProcessingException {
        PrivateChannelResponse privateChannel = new PrivateChannelResponse(
                UUID.randomUUID().toString(),
                Set.of("king_gustaf.bouvetinterchange.eu", "king_olav.bouvetinterchange.eu"),
                PrivateChannelStatus.CREATED,
                "private channel for gustaf and olav",
                new PrivateChannelEndpoint("a.bouvetinterchange.eu", 1337, "priv-" + UUID.randomUUID()),
                98521521L
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(privateChannel));
    }

    @Test
    public void getPeerPrivateChannelsResponse() throws JsonProcessingException {
        List<PeerPrivateChannel> peerPrivateChannels = new ArrayList<>();
        peerPrivateChannels.add(
                new PeerPrivateChannel(
                        UUID.randomUUID().toString(),
                        "king_olav.bouvetinterchange.eu",
                        PrivateChannelStatus.CREATED,
                        "private channel between king_olav and king_gustaf",
                        new PrivateChannelEndpoint("a.bouvetinterchange.eu", 1337, "priv-" + UUID.randomUUID()),
                        948212421L
                )
        );
        peerPrivateChannels.add(
                new PeerPrivateChannel(
                        UUID.randomUUID().toString(),
                        "king_bjarne",
                        PrivateChannelStatus.CREATED,
                        "private channel between king_bjarne and king_gustaf",
                        new PrivateChannelEndpoint("a.bouvetinterchange.eu", 1337, "priv-" + UUID.randomUUID()),
                        948212421L
                )
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(peerPrivateChannels));
    }

    @Test
    public void getPeerPrivateChannelResponse() throws JsonProcessingException {
        PeerPrivateChannel peerPrivateChannel = new PeerPrivateChannel(
                UUID.randomUUID().toString(),
                "king_bjarne",
                PrivateChannelStatus.CREATED,
                "private channel between king_bjarne and king_gustaf",
                new PrivateChannelEndpoint("a.bouvetinterchange.eu", 1337, "priv-" + UUID.randomUUID()),
                948212421L
        );
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(peerPrivateChannel));
    }

    @Test
    public void addPeerToPrivateChannelRequest() throws JsonProcessingException {
        AddPeerRequest peerRequest = new AddPeerRequest("king_olav.bouvetinterchange.eu");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(peerRequest));
    }

}
