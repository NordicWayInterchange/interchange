import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OnboardRestAPIDocumentationTest {



    @Test
    public void addSingleSubscriptionTest() throws JsonProcessingException {
        Set<SelectorApi> selectors = new HashSet<>();
        selectors.add(new SelectorApi("countryCode = 'SE' and messageType = 'DENM' and quadTree like '%,12003%'"));
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                "kyrre",
                selectors
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void addSubscriptionRequest() throws JsonProcessingException {
        Set<SelectorApi> selectors = new HashSet<>();
        selectors.add(new SelectorApi("countryCode = 'NO' and messageType = 'DENM'"));
        selectors.add(new SelectorApi("countryCode = 'SE' and messageType = 'DENM'"));
        AddSubscriptionsRequest request = new AddSubscriptionsRequest(
                "serviceprovider1",
                selectors
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
    }

    @Test
    public void addSubscriptionsResponse() throws JsonProcessingException {
        Set<LocalActorSubscription> subscriptions = new HashSet<>();
        subscriptions.add(new LocalActorSubscription("1",
                "/serviceprovider1/subscriptions/1",
                "countryCode = 'NO' and messageType = 'DENM'",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.REQUESTED));
        subscriptions.add(new LocalActorSubscription("2",
                "/serviceprovider1/subscriptions/2",
                "countryCode = 'SE' and messageType = 'DENM'",
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
                "countryCode = 'NO' and messageType = 'DENM'",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED));
        subscriptions.add(new LocalActorSubscription("2",
                "/serviceprovider1/subscriptions/2",
                "countryCode = 'SE' and messageType = 'DENM'",
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
        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.add(new Endpoint(
                "amqps://myserver",
                "serviceprovider1-1",
                0,
                0
        ));
        GetSubscriptionResponse response = new GetSubscriptionResponse(
                "1",
                "/serviceprovider1/subscriptions/1",
                "countryCode = 'NO' and messageType = 'DENM'",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED,
                endpoints

        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    //TODO
    @Test
    public void createDENMCapability() throws JsonProcessingException {
        DenmCapabilityApi api = new DenmCapabilityApi(
                "NPRA",
                "NO",
                "1.0",
                Collections.singleton("1234"),
                Collections.singleton("6")

        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api));

    }

    @Test
    public void addCapabilitiesRequest() throws JsonProcessingException {
        AddCapabilitiesRequest request = new AddCapabilitiesRequest(
                "sp-1",
                Collections.singleton(
                        new DenmCapabilityApi(
                                "NPRA",
                                "NO",
                                "1.0",
                                Collections.singleton("1234"),
                                Collections.singleton("6")
                        )
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
                                new DenmCapabilityApi(
                                        "NPRA",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton("6")
                                )
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
                                new DenmCapabilityApi(
                                        "NPRA",
                                        "NO",
                                        "1.0",
                                        Collections.singleton("1234"),
                                        Collections.singleton("6")
                                )
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
                new DenmCapabilityApi(
                        "NPRA",
                        "NO",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                )
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

    }
}
