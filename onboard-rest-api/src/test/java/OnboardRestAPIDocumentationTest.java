import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;

public class OnboardRestAPIDocumentationTest {



    @Test
    public void addSingleSubscriptionTest() throws JsonProcessingException {
        Set<AddSubscription> addSubscriptions = new HashSet<>();
        addSubscriptions.add(new AddSubscription("originatingCountry = 'SE' and messageType = 'DENM' and quadTree like '%,12003%'", "kyrre"));
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                "kyrre",
                addSubscriptions
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void addSubscriptionRequest() throws JsonProcessingException {
        Set<AddSubscription> addSubscriptions = new HashSet<>();
        addSubscriptions.add(new AddSubscription("originatingCountry = 'NO' and messageType = 'DENM'"));
        addSubscriptions.add(new AddSubscription("originatingCountry = 'SE' and messageType = 'DENM'"));
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                "serviceprovider1",
                addSubscriptions
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void addSingleSubscriptionForSystemTest() throws JsonProcessingException {
        //TODO for local
        Set<AddSubscription> addSubscriptions = new HashSet<>();
        addSubscriptions.add(new AddSubscription("originatingCountry = 'SE' and messageType = 'DENM'"));
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                "king_olav.bouvetinterchange.eu",
                addSubscriptions
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }


    @Test
    public void addSubscriptionsResponse() throws JsonProcessingException {
        Set<LocalActorSubscription> subscriptions = new HashSet<>();
        subscriptions.add(new LocalActorSubscription("1",
                "/serviceprovider1/subscriptions/1",
                "originatingCountry = 'NO' and messageType = 'DENM'",
                "serviceprovider1",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.REQUESTED,
                null));
        subscriptions.add(new LocalActorSubscription("2",
                "/serviceprovider1/subscriptions/2",
                "originatingCountry = 'SE' and messageType = 'DENM'",
                "serviceprovider1",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.REQUESTED,
                null
                ));
        AddSubscriptionsResponse response = new AddSubscriptionsResponse(
                "serviceprovider1",
                subscriptions

        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

    }

    @Test
    public void listSubscriptionsResponse() throws JsonProcessingException {
        Set<LocalActorSubscription> subscriptions = new HashSet<>();
        subscriptions.add(new LocalActorSubscription("1",
                "/serviceprovider1/subscriptions/1",
                "originatingCountry = 'NO' and messageType = 'DENM'",
                "serviceprovider1",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED,
                null));
        subscriptions.add(new LocalActorSubscription("2",
                "/serviceprovider1/subscriptions/2",
                "originatingCountry = 'SE' and messageType = 'DENM'",
                "serviceprovider1",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED,
                null
        ));
        ListSubscriptionsResponse response = new ListSubscriptionsResponse(
                "serviceprovider1",
                subscriptions

        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

    }

    @Test
    public void getSubscriptionResponse() throws JsonProcessingException {
        Set<LocalEndpointApi> localEndpointApis = new HashSet<>();
        localEndpointApis.add(new LocalEndpointApi(
                "amqps://myserver",
                5671,
                "serviceprovider1-1",
                0,
                0
        ));
        GetSubscriptionResponse response = new GetSubscriptionResponse(
                "1",
                "/serviceprovider1/subscriptions/1",
                "originatingCountry = 'NO' and messageType = 'DENM'",
                "serviceprovider1",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED,
                localEndpointApis

        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void createDENMCapability() throws JsonProcessingException {
        CapabilitySplitApi api = new CapabilitySplitApi(
                new DenmApplicationApi(
                "NPRA",
                "pub-1",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton(6)),
                new MetadataApi(RedirectStatusApi.OPTIONAL)
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));

    }

    @Test
    public void addCapabilitiesRequest() throws JsonProcessingException {
        AddCapabilitiesRequest request = new AddCapabilitiesRequest(
                "sp-1",
                Collections.singleton(
                        new CapabilitySplitApi(
                                new DenmApplicationApi(
                                        "NPRA",
                                        "pub-1",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton(6)
                                ), new MetadataApi())
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));

    }

    @Test
    public void addCapabilitiesRequestForSystemtest() throws JsonProcessingException {
        AddCapabilitiesRequest request = new AddCapabilitiesRequest(
                "king_gustaf.bouvetinterchange.eu",
                Collections.singleton(
                        new CapabilitySplitApi(
                                new DenmApplicationApi(
                                        "SVT",
                                        "pub-1",
                                        "SE",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton(6)
                                ), new MetadataApi(RedirectStatusApi.OPTIONAL))
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));

    }

    @Test
    public void addCapabilitiesResponse() throws JsonProcessingException {
        AddCapabilitiesResponse response = new AddCapabilitiesResponse(
                "sp-1",
                Collections.singleton(
                        new LocalActorCapability(
                                "1",
                                "/sp-1/capabilities/1",
                                new CapabilitySplitApi(
                                    new DenmApplicationApi(
                                        "NPRA",
                                        "pub-1",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton(6)
                                ), new MetadataApi())
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void listCapabilitiesResponse() throws JsonProcessingException {
        ListCapabilitiesResponse response = new ListCapabilitiesResponse(
                "sp-1",
                Collections.singleton(
                        new LocalActorCapability(
                                "1",
                                "/spi-1/capabilities/1",
                                new CapabilitySplitApi(
                                        new DenmApplicationApi(
                                                "NPRA",
                                                "pub-1",
                                                "NO",
                                                "1.0",
                                                Collections.singleton("1234"),
                                                Collections.singleton(6)
                                        ), new MetadataApi())
                        )
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void getCapabilityResponse() throws JsonProcessingException {
        GetCapabilityResponse response = new GetCapabilityResponse(
                "1",
                "/sp-1/capabilities/1",
                new CapabilitySplitApi(
                        new DenmApplicationApi(
                                "NPRA",
                                "pub-1",
                                "NO",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton(6)
                        ), new MetadataApi())
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

    }

    @Test
    public void addDelieriesRequest() throws JsonProcessingException {
        AddDeliveriesRequest request = new AddDeliveriesRequest(
                "sp-1",
                Collections.singleton(new SelectorApi(
                        "originatingCountry = 'NO' and messageType = 'DENM'"
                ))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void addDeliveriesResponse() throws JsonProcessingException {
        AddDeliveriesResponse response = new AddDeliveriesResponse(
                "sp-1",
                Collections.singleton(new Delivery(
                        "1",
                        "/sp-1/deliveries/1",
                        "originatingCountry = 'NO' and messageType = 'DENM'",
                        System.currentTimeMillis(),
                        DeliveryStatus.REQUESTED,
                        null
                ))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void listDeliveriesResponse() throws JsonProcessingException {
        ListDeliveriesResponse response = new ListDeliveriesResponse(
                "sp-1",
                Collections.singleton(new Delivery(
                        "1",
                        "/sp-1/deliveries/1",
                        "originatingCountry = 'NO' and messageType = 'DENM'",
                        System.currentTimeMillis(),
                        DeliveryStatus.CREATED,
                        null

                ))
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }


    @Test
    public void getDeliveryResponse() throws JsonProcessingException {
        GetDeliveryResponse response = new GetDeliveryResponse(
                "1",
                Collections.singleton(new DeliveryEndpoint(
                        "amqps://sp-1",
                        5671,
                        "sp1-1"
                )),
                "/sp-1/deliveries/1",
                "originatingCountry = 'NO' and messageType = 'DENM'",
                System.currentTimeMillis(),
                DeliveryStatus.CREATED
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void fetchMatchingCapabilitiesResponseWithoutSelector() throws JsonProcessingException {
        FetchMatchingCapabilitiesResponse response = new FetchMatchingCapabilitiesResponse(
                "service-provider",
                Collections.singleton(
                        new CapabilitySplitApi(
                                new DenmApplicationApi(
                                        "NPRA",
                                        "pub-1",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton(6)
                                ), new MetadataApi())
                )
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void fetchMatchingCapabilitiesResponseWithSelector() throws JsonProcessingException {
        FetchMatchingCapabilitiesResponse response = new FetchMatchingCapabilitiesResponse(
                "service-provider",
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like 'quadTree like '%,0123%'",
                Collections.singleton(
                        new CapabilitySplitApi(
                                new DenmApplicationApi(
                                        "NPRA",
                                        "pub-1",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton(6)
                                ), new MetadataApi())
                        )

        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void selectorApi() throws JsonProcessingException {
        SelectorApi selector = new SelectorApi("originatingCountry = 'NO' and messageType = 'DENM' and quadTree like 'quadTree like '%,0123%'");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(selector));
    }

    @Test
    public void addPrivateChannelApi() throws JsonProcessingException {
        PrivateChannelResponseApi api = new PrivateChannelResponseApi();
        api.setPeerName("sp2");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(api));
    }
    @Test
    public void AddPrivateChannelRequest() throws JsonProcessingException {
        PrivateChannelRequestApi privateChannel = new PrivateChannelRequestApi("king_olaf.bouvetinterchange.eu");
        AddPrivateChannelRequest request = new AddPrivateChannelRequest("king_gustaf.bouvetinterchange.eu",List.of(privateChannel));
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void addPrivateChannelResponse() throws JsonProcessingException {
        PrivateChannelResponseApi privateChannel = new PrivateChannelResponseApi("king_olaf.bouvetinterchange.eu", PrivateChannelStatusApi.REQUESTED, 1);
        AddPrivateChannelResponse response = new AddPrivateChannelResponse();
        response.setName("king_gustaf.bouvetinterchange.eu");
        response.getPrivateChannels().add(privateChannel);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
    @Test
    public void getPrivateChannelResponse() throws JsonProcessingException {
        PrivateChannelEndpointApi endpoint = new PrivateChannelEndpointApi("hostname",5671,"550e8400-e29b-41d4-a716-446655440000");
        GetPrivateChannelResponse response = new GetPrivateChannelResponse(1, "king_olaf.bouvetinterchange.eu",endpoint,"king_gustaf.bouvetinterchange.eu", PrivateChannelStatusApi.CREATED);
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
    @Test
    public void ListPrivateChannelsResponse()throws JsonProcessingException{
        PrivateChannelEndpointApi endpoint = new PrivateChannelEndpointApi("hostname",5671,"550e8400-e29b-41d4-a716-446655440000");
        PrivateChannelResponseApi privateChannel = new PrivateChannelResponseApi("king_olaf.bouvetinterchange.eu",PrivateChannelStatusApi.CREATED,endpoint,1);
        ListPrivateChannelsResponse response = new ListPrivateChannelsResponse("king_gustaf.bouvetinterchange.eu", List.of(privateChannel));
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
    @Test
    public void ListPeerPrivateChannels() throws JsonProcessingException {
        PrivateChannelEndpointApi endpoint = new PrivateChannelEndpointApi("hostname",5671,"550e8400-e29b-41d4-a716-446655440000");
        PeerPrivateChannelApi privateChannel = new PeerPrivateChannelApi(1,"king_olaf.bouvetinterchange.eu" ,PrivateChannelStatusApi.CREATED, endpoint);
        ListPeerPrivateChannels response = new ListPeerPrivateChannels("king_gustaf.bouvetinterchange.eu", List.of(privateChannel));
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    @Test
    public void addMultipleCapabilitiesTest() throws JsonProcessingException {
        Set<String> quadTree = Collections.singleton("12004");
        MetadataApi metadataApi = new MetadataApi(
                1,
                "info.com",
                RedirectStatusApi.OPTIONAL,
                0,
                0,
                0
        );

        CapabilitySplitApi denm = new CapabilitySplitApi(
                new DenmApplicationApi(
                        "NO00000",
                        "NO00000-DENM",
                        "NO",
                        "DENM:1.2.2",
                        quadTree,
                        Collections.singleton(6)
                ),
                metadataApi
        );

        CapabilitySplitApi datex = new CapabilitySplitApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000-DATEX",
                        "NO",
                        "DATEX2:2.3",
                        quadTree,
                        "SituationPublication"
                ),
                metadataApi
        );

        CapabilitySplitApi ivim = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-IVIM",
                        "NO",
                        "IVI:1.2",
                        quadTree),
                metadataApi
        );

        CapabilitySplitApi spatem = new CapabilitySplitApi(
                new SpatemApplicationApi(
                        "NO00000",
                        "NO00000-SPATEM",
                        "NO",
                        "SPATEM",
                        quadTree
                ),
                metadataApi
        );

        CapabilitySplitApi mapem = new CapabilitySplitApi(
                new MapemApplicationApi(
                        "NO00000",
                        "NO00000-MAPEM",
                        "NO",
                        "MAPEM",
                        quadTree
                ),
                metadataApi
        );

        CapabilitySplitApi ssem = new CapabilitySplitApi(
                new SsemApplicationApi(
                        "NO00000",
                        "NO00000-SSEM",
                        "NO",
                        "SSEM",
                        quadTree
                ),
                metadataApi
        );

        CapabilitySplitApi srem = new CapabilitySplitApi(
                new SremApplicationApi(
                        "NO00000",
                        "NO00000-SREM",
                        "NO",
                        "SREM",
                        quadTree
                ),
                metadataApi
        );

        CapabilitySplitApi cam = new CapabilitySplitApi(
                new CamApplicationApi(
                        "NO00000",
                        "NO00000-CAM",
                        "NO",
                        "CAM",
                        quadTree
                ),
                metadataApi
        );

        AddCapabilitiesRequest request = new AddCapabilitiesRequest(
                "king_olav",
                new HashSet<>(Arrays.asList(
                        denm,
                        datex,
                        ivim,
                        spatem,
                        mapem,
                        ssem,
                        srem,
                        cam
                ))
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

}
