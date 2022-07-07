import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.serviceprovider.model.*;
import no.vegvesen.ixn.serviceprovider.model.DeliveryStatus;
import no.vegvesen.ixn.serviceprovider.model.LocalActorSubscription;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class TypeTransformer {




    public static List<String> makeHostAndPortOfUrl(String url){
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1){
            port = 5671;
        }
        return new ArrayList<>(Arrays.asList(host, String.valueOf(port)));
    }

    public ListSubscriptionsResponse transformLocalSubscriptionsToListSubscriptionResponse(String name, Set<LocalSubscription> subscriptions) {
        return new ListSubscriptionsResponse(name,transformLocalSubscriptionsToLocalActorSubscription(name,subscriptions));
    }

    private Set<LocalActorSubscription> transformLocalSubscriptionsToLocalActorSubscription(String name, Set<LocalSubscription> subscriptions) {
        Set<LocalActorSubscription> result = new HashSet<>();
        for (LocalSubscription subscription : subscriptions) {
            String sub_id = subscription.getId() == null ? null : subscription.getId().toString();
            result.add(new LocalActorSubscription(
                    sub_id,
                    createSubscriptionPath(name,sub_id),
                    subscription.getSelector(),
                    transformLocalDateTimeToEpochMili(subscription.getLastUpdated()),
                    transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(subscription.getStatus())));
        }
        return result;
    }

    public ListCapabilitiesResponse listCapabilitiesResponse(String serviceProviderName, Set<Capability> capabilities) {
        return new ListCapabilitiesResponse(
                serviceProviderName,
                capabilitySetToLocalActorCapability(serviceProviderName,capabilities)
        );
    }

    private static Set<LocalActorCapability> capabilitySetToLocalActorCapability(String serviceProviderName, Set<Capability> capabilities) {
        Set<LocalActorCapability> result = new HashSet<>();
        for (Capability capability : capabilities) {
            result.add(capabilityToLocalCapability(serviceProviderName,capability));
        }
        return result;
    }

    private static LocalActorCapability capabilityToLocalCapability(String serviceProviderName, Capability capability) {
        String id = capability.getId().toString();
        return new LocalActorCapability(
                id,
                createCapabilitiesPath(serviceProviderName, id),
                capability.toApi());
    }







    private String createSubscriptionPath(String serviceProviderName, String subscriptionId) {
        return String.format("/%s/subscriptions/%s", serviceProviderName, subscriptionId);
    }

    private long transformLocalDateTimeToEpochMili(LocalDateTime lastUpdated) {
        return lastUpdated == null ? 0 : ZonedDateTime.of(lastUpdated, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static String createCapabilitiesPath(String serviceProviderName, String capabilityId) {
        return String.format("/%s/capabilities/%s", serviceProviderName, capabilityId);
    }

    private LocalActorSubscriptionStatusApi transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(LocalSubscriptionStatus status) {
        switch (status) {
            //case ASSIGN_QUEUE:
            case REQUESTED:
                return LocalActorSubscriptionStatusApi.REQUESTED;
            case CREATED:
                return LocalActorSubscriptionStatusApi.CREATED;
            case TEAR_DOWN:
                return LocalActorSubscriptionStatusApi.NOT_VALID;
            default:
                return LocalActorSubscriptionStatusApi.ILLEGAL;
        }
    }
    public ListDeliveriesResponse transformToListDeliveriesResponse(String serviceProviderName, Set<LocalDelivery> localDeliveries) {
        return new ListDeliveriesResponse(serviceProviderName,
                transformLocalDeliveryToDelivery(
                        serviceProviderName,
                        localDeliveries
                )
        );
    }

    public Set<Delivery> transformLocalDeliveryToDelivery(String serviceProviderName, Set<LocalDelivery> localDeliveries) {
        Set<Delivery> result = new HashSet<>();
        for (LocalDelivery delivery : localDeliveries){
            result.add(new Delivery(
                            delivery.getId().toString(),
                            createDeliveryPath(serviceProviderName, delivery.getId().toString()),
                            delivery.getSelector(),
                            transformLocalDateTimeToEpochMili(delivery.getLastUpdatedTimestamp()),
                            transformLocalDeliveryStatusToDeliveryStatus(delivery.getStatus())
                    )
            );
        }
        return result;
    }
    private static String createDeliveryPath(String serviceProviderName, String deliveryId) {
        return String.format("/%s/deliveries/%s", serviceProviderName, deliveryId);
    }
    private DeliveryStatus transformLocalDeliveryStatusToDeliveryStatus(LocalDeliveryStatus status) {
        switch (status) {
            case REQUESTED:
                return DeliveryStatus.REQUESTED;
            case CREATED:
                return DeliveryStatus.CREATED;
            case NOT_VALID:
                return DeliveryStatus.NOT_VALID;
            default:
                return DeliveryStatus.ILLEGAL;
        }
    }

    public boolean isNeighbourReachable (NeighbourRepository neighbourRepository, Neighbour neighbourName) {

        List<Neighbour> tempListOfNeighbours = neighbourRepository.findAll();

        for (Neighbour n :
                tempListOfNeighbours) {
            if (n.getName().equals(neighbourName)) {
                return true;
            }
        }

        return true;
    }

    public GetAllNeighboursResponse getAllNeighboursResponse(String interchangeName,NeighbourRepository neighbourRepository){
        Set<NeighbourWithPathAndApi> neighbours = new HashSet<>();
        for(Neighbour neighbour : neighbourRepository.findAll()){
            neighbours.add(
                    new NeighbourWithPathAndApi(neighbour.getNeighbour_id().toString(), neighbourPath(neighbour.getName()), transformToNeighbourStatusApi(isNeighbourReachable(neighbourRepository, neighbour))));
        }
        return new GetAllNeighboursResponse(interchangeName, neighbours);
    }

    private static String neighbourPath(String neighbourName) {
        return String.format("/neighbour/%s", neighbourName);
    }

    public ListNeighbourCapabilitiesResponse listNeighbourCapabilitiesResponse(Neighbour neighbourName, Set<Capability> capabilities) {
        return new ListNeighbourCapabilitiesResponse(neighbourName.getName(),
                capabilitySetToNeighbourCapabilityApi(neighbourName.getName(),capabilities)
        );
    }

    private static Set<NeighbourCapabilityApi> capabilitySetToNeighbourCapabilityApi(String neighbourName, Set<Capability> capabilities) {
        Set<NeighbourCapabilityApi> result = new HashSet<>();
        for (Capability capability : capabilities) {
            String capabilityId = capability.getId().toString();
            result.add(
                    new NeighbourCapabilityApi(capabilityId,
                            createNeighbourCapabilityPath(neighbourName, capabilityId),capability.toApi())
            );
        }
        return result;
    }

    private static String createNeighbourCapabilityPath(String neighbourName, String capibilityId) {
        return String.format("/neighbour/%s/capabilities/%s", neighbourName, capibilityId);
    }

   public ListNeighbourSubscriptionResponse transformOurAndTheirSubscriptionsToListSubscriptionResponse(String neighbourName, Set<Subscription> ourSubscriptions, Set<Subscription> theirSubscriptions){
        Set<NeighbourRequestedSubscriptionApi> neighbourRequestedSubscriptionApis = new HashSet<>();
        Set<OurRequestedSubscriptionApi> ourRequestedSubscriptionApi = new HashSet<>();
        for(Subscription subscription : ourSubscriptions){
            String sub_id = subscription.getId() == null ? null : subscription.getId().toString();
            ourRequestedSubscriptionApi.add(new OurRequestedSubscriptionApi(
                    sub_id,
                    subscription.getPath(),
                    subscription.getPath(),
                    subscription.getLastUpdatedTimestamp()));

        }
       for(Subscription subscription : theirSubscriptions){
           String sub_id = subscription.getId() == null ? null : subscription.getId().toString();
           neighbourRequestedSubscriptionApis.add(new NeighbourRequestedSubscriptionApi(
                   sub_id,
                   subscription.getPath(),
                   subscription.getPath(),
                   subscription.getLastUpdatedTimestamp()));

       }
       ListNeighbourSubscriptionResponse response = new ListNeighbourSubscriptionResponse(neighbourName, neighbourRequestedSubscriptionApis, ourRequestedSubscriptionApi);
        return response;
   }

    private NeighbourStatusApi transformToNeighbourStatusApi(boolean status) {
        if (status == true) {
            return NeighbourStatusApi.UP;
        }else{
            return NeighbourStatusApi.DOWN;
        }
    }

}
