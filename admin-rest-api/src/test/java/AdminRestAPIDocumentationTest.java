import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmCapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class AdminRestAPIDocumentationTest {


    @Autowired
    ServiceProvider serviceProvider = generateServiceProvider();


    //getAllNeighbours()
    @Disabled
    @Test
    public void GetAllNeighboursResponse() throws JsonProcessingException {
        Set<NeighbourWithPathAndApi> neighbours = new HashSet<>();
        neighbours.add(new NeighbourWithPathAndApi("1",
                "/neighbour1",
                NeighbourStatusApi.UP));
        neighbours.add(new NeighbourWithPathAndApi("2",
                "/neighbour2",
                NeighbourStatusApi.DOWN
        ));
        GetAllNeighboursResponse response = new GetAllNeighboursResponse(
                neighbours
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

    }


    //getCapabilitiesFromNeighbour
    @Test
    public void ListNeighbourCapabilitiesResponse() throws JsonProcessingException {
        Set<NeighbourCapabilityApi> neighbourCapabilities = new HashSet<>();
        neighbourCapabilities.add(new NeighbourCapabilityApi("1",
                "/neighbour1/capabilities/1",
                new DenmCapabilityApi("NPRA",
                        "NO",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("6")
                )
        ));
        neighbourCapabilities.add(new NeighbourCapabilityApi("2",
                "/neighbour1/capabilities/2",
                new DenmCapabilityApi(
                        "NPRA",
                        "NO",
                        "1.0",
                        Collections.singleton("1234"),
                        Collections.singleton("7")
                )
        ));
        ListNeighbourCapabilitiesResponse response = new ListNeighbourCapabilitiesResponse(
                "Norge",
                neighbourCapabilities
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

    }

    //getSubscriptionsFromNeighbour
    @Test
    public void ListNeighbourSubscriptionsResponse() throws JsonProcessingException {
        Set<NeighbourRequestedSubscriptionApi> neighbourSubscriptions = new HashSet<>();
        Set<OurRequestedSubscriptionApi> ourRequestedSubscriptionApi = new HashSet<>();

        ourRequestedSubscriptionApi.add(new OurRequestedSubscriptionApi(
                "1",
                "/neighbour1/subscriptions/1",
                "originatingCountry = 'NO' and messageType = 'DENM'",
                System.currentTimeMillis()
        ));
        neighbourSubscriptions.add(new NeighbourRequestedSubscriptionApi(
                "1",
                "/neighbour2/subscriptions/1",
                "originatingCountry = 'SE' and messageType = 'DENM'",
                System.currentTimeMillis()
        ));
        ListNeighbourSubscriptionResponse response = new ListNeighbourSubscriptionResponse(
                "Norge",
                neighbourSubscriptions,
                ourRequestedSubscriptionApi
        );

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

    }

    //isNeighbourReachable

    //getAllServiceProviders

    @Test
    public void getServiceProviderTest() throws JsonProcessingException{
        GetServiceProviderResponse getServiceProviderRequest = new GetServiceProviderResponse(serviceProvider);

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getServiceProviderRequest));


    }

    @Test
    public void getAllServiceProvidersTest()throws JsonProcessingException{

        GetAllServiceProvidersResponse getAllServiceProvidersResponse = new GetAllServiceProvidersResponse(generateMultipleServiceProviders(5));

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(getAllServiceProvidersResponse));


    }

    //getCapabilitiesFromServiceProvider
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









    //getDeliveriesFromServiceProvider
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


    //getSubscriptionsFromServiceProvider
    @Test
    public void listSubscriptionsResponse() throws JsonProcessingException {
        Set<LocalActorSubscription> subscriptions = new HashSet<>();
        subscriptions.add(new LocalActorSubscription("1",
                "/serviceprovider1/subscriptions/1",
                "originatingCountry = 'NO' and messageType = 'DENM'",
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED));
        subscriptions.add(new LocalActorSubscription("2",
                "/serviceprovider1/subscriptions/2",
                "originatingCountry = 'SE' and messageType = 'DENM'",
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
                System.currentTimeMillis(),
                LocalActorSubscriptionStatusApi.CREATED,
                localEndpointApis

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
                        "sp1-1",
                        "originatingCountry = 'NO' and messageType = 'DENM'"
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
    public void selectorApi() throws JsonProcessingException {
        SelectorApi selector = new SelectorApi("originatingCountry = 'NO' and messageType = 'DENM' and quadTree like 'quadTree like '%,0123%'");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(selector));
    }

    public ServiceProvider generateServiceProvider (){
        String name = "sp-1";
        Capabilities capabilities = new Capabilities();

        Set<LocalSubscription> subscriptions = new HashSet<LocalSubscription>();


        ServiceProvider serviceProvider = new ServiceProvider(name);
        serviceProvider.setId(1);
        serviceProvider.setCapabilities(capabilities);
        serviceProvider.setDeliveries(new HashSet<LocalDelivery>());
        serviceProvider.setSubscriptions(subscriptions);

        return serviceProvider;

   }
   public List<ServiceProvider> generateMultipleServiceProviders(int howMany){
        List<ServiceProvider> serviceProviders = new ArrayList<>();

       for (int i = 0; i < howMany; i++) {
           String name = "sp-" + Integer.toString(i);

           Capabilities capabilities = new Capabilities();

           Set<LocalSubscription> subscriptions = new HashSet<LocalSubscription>();


           ServiceProvider serviceProvider = new ServiceProvider(name);
           serviceProvider.setId(i);
           serviceProvider.setCapabilities(capabilities);
           serviceProvider.setDeliveries(new HashSet<LocalDelivery>());
           serviceProvider.setSubscriptions(subscriptions);
           serviceProviders.add(serviceProvider);

       }
       return serviceProviders;

   }
}
