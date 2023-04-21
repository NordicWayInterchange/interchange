import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.RedirectStatusApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
                LocalActorSubscriptionStatusApi.REQUESTED));
        subscriptions.add(new LocalActorSubscription("2",
                "/serviceprovider1/subscriptions/2",
                "originatingCountry = 'SE' and messageType = 'DENM'",
                "serviceprovider1",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.REQUESTED
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
                LocalActorSubscriptionStatusApi.CREATED));
        subscriptions.add(new LocalActorSubscription("2",
                "/serviceprovider1/subscriptions/2",
                "originatingCountry = 'SE' and messageType = 'DENM'",
                "serviceprovider1",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED
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
        //TODO for remote
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
                                ), new MetadataApi())
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
                        DeliveryStatus.REQUESTED
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
                        DeliveryStatus.CREATED

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
                        new FetchCapability(
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
    public void fetchMatchingCapabilitiesResponseWithSelector() throws JsonProcessingException {
        FetchMatchingCapabilitiesResponse response = new FetchMatchingCapabilitiesResponse(
                "service-provider",
                "originatingCountry = 'NO' and messageType = 'DENM' and quadTree like 'quadTree like '%,0123%'",
                Collections.singleton(
                        new FetchCapability(
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
    public void selectorApi() throws JsonProcessingException {
        SelectorApi selector = new SelectorApi("originatingCountry = 'NO' and messageType = 'DENM' and quadTree like 'quadTree like '%,0123%'");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(selector));
    }

    @Test
    public void addPrivateChannelApi() throws JsonProcessingException {
        PrivateChannelApi api = new PrivateChannelApi();
        api.setPeerName("sp2");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(api));
    }
}
