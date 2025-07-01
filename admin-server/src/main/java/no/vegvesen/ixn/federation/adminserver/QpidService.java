package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.adminserver.model.endpoint.LocalDeliveryEndpointAdminApi;
import no.vegvesen.ixn.federation.adminserver.model.endpoint.LocalSubscriptionEndpointAdminApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilitiesLinkedDeliveryApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilitiesLinkedSubscriptionApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilityMatchApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.LocalDeliveryEndpointApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.LocalSubscriptionEndpointApi;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QpidService {

    private final AdminQpidClient adminQpidClient;

    @Autowired
    public QpidService(AdminQpidClient adminQpidClient) {
        this.adminQpidClient = adminQpidClient;
    }

    public boolean exchangeExists(String exchangeName) {
        return adminQpidClient.exchangeExists(exchangeName);
    }

    public boolean queueExists(String queueName) {
        return adminQpidClient.queueExists(queueName);
    }

    public boolean bindingExists(String exchangeName, String queueName) {
        Exchange exchange = adminQpidClient.getExchange(exchangeName);
        if (exchange != null) {
            return exchange.isBoundToQueue(queueName);
        } else {
            return false;
        }
    }

    public String joinTwoSelectors(String firstSelector, String secondSelector) {
        return String.format("(%s) AND (%s)", firstSelector, secondSelector);
    }

    public List<LocalDeliveryEndpointAdminApi> getLocalDeliveryEndpointApiList(LocalDelivery delivery) {
        List<LocalDeliveryEndpointAdminApi> result = new ArrayList<>();
        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
            Exchange exchange = adminQpidClient.getExchange(endpoint.getTarget());
            result.add(new LocalDeliveryEndpointAdminApi(
                    new LocalDeliveryEndpointApi(
                            endpoint.getHost(),
                            endpoint.getPort(),
                            endpoint.getTarget()
                    ),
                    exchange != null
                    )
            );

        }
        return result;
    }


    public List<LocalSubscriptionEndpointAdminApi> getLocalSubscriptionEndpointApiList(LocalSubscription localSubscription) {
        List<LocalSubscriptionEndpointAdminApi> result = new ArrayList<>();
        for (LocalEndpoint endpoint : localSubscription.getLocalEndpoints()) {
            Exchange exchange = adminQpidClient.getExchange(endpoint.getHost());
            result.add(new LocalSubscriptionEndpointAdminApi(
                            new LocalSubscriptionEndpointApi(
                                    endpoint.getId(),
                                    endpoint.getSource(),
                                    endpoint.getHost(),
                                    endpoint.getPort(),
                                    endpoint.getMaxBandwidth(),
                                    endpoint.getMaxMessageRate()
                            ),
                            exchange != null
                    )
            );

        }
        return result;
    }

    public CapabilitiesLinkedDeliveryApi getCapabilitiesLinkedDelivery(LocalDelivery delivery, List<OutgoingMatch> matches) {
        String uuid = delivery.getUuid();
        List<CapabilityMatchApi> capabilityMatches = new ArrayList<>();
        for (OutgoingMatch match : matches) {
            Capability capability = match.getCapability();
            for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                for (CapabilityShard shard : capability.getShards()) {
                    boolean exists;
                    String exchangeName = endpoint.getTarget();
                    String queueName = shard.getExchangeName();
                    Exchange exchange = adminQpidClient.getExchange(exchangeName);
                    Binding binding;
                    if (exchange != null) {
                        binding = exchange.getBindingTo(queueName);
                        exists = binding != null;
                    } else {

                        String joinedSelector = joinTwoSelectors(shard.getSelector(), delivery.getSelector());
                        binding = new Binding(shard.getExchangeName(), endpoint.getTarget(), new Filter(joinedSelector));
                        exists = false;
                    }
                    CapabilityMatchApi matchApi = new CapabilityMatchApi(
                            capability.getUuid(),
                            shard.getShardId(),
                            binding,
                            exists
                    );
                    capabilityMatches.add(matchApi);
                }
            }
        }
        return new CapabilitiesLinkedDeliveryApi(uuid,capabilityMatches);
    }

    public CapabilitiesLinkedSubscriptionApi getCapabilitiesLinkedSubscription(LocalSubscription subscription, Set<Capability> localCratedCapability) {
        Set<Capability> allMatchingLocalCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(localCratedCapability,subscription.getSelector());
        List<CapabilityMatchApi> capabilityMatches = new ArrayList<>();
        //TODO we don't know which endpoint is mapped to which shard...
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String subscriptionEndpoint = endpoint.getSource();
            for (Capability capability : allMatchingLocalCapabilities) {
                String capabilityUuid = capability.getUuid();
                for (CapabilityShard shard : capability.getShards()) {
                    Exchange exchange = adminQpidClient.getExchange(shard.getExchangeName());
                    Binding binding;
                    boolean exists;
                    if (exchange != null) {
                        binding = exchange.getBindingTo(subscriptionEndpoint);
                        exists = binding != null;
                    } else {
                        binding = new Binding(shard.getExchangeName(), subscriptionEndpoint, new Filter(subscription.getSelector()));
                        exists = false;
                    }
                    capabilityMatches.add(
                            new CapabilityMatchApi(
                                    capabilityUuid,
                                    shard.getShardId(),
                                    binding,
                                    exists
                            )
                    );
                }
            }
        }
        return new CapabilitiesLinkedSubscriptionApi(subscription.getUuid(),capabilityMatches);
    }



    public List<Exchange> getAllExchanges() {
        try {
            return adminQpidClient.getAllExchanges();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Queue> getAllQueues() {
        try {
            return adminQpidClient.getAllQueues();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
