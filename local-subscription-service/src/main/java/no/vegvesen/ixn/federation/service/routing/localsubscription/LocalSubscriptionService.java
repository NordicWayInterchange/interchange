package no.vegvesen.ixn.federation.service.routing.localsubscription;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class LocalSubscriptionService {

    private static Logger logger = LoggerFactory.getLogger(LocalSubscriptionService.class);

    private final QpidClient qpidClient;
    private final ServiceProviderRepository repository;
    private final MatchRepository matchRepository;

    @Autowired
    public LocalSubscriptionService(ServiceProviderRepository repository, MatchRepository matchRepository, QpidClient qpidClient) {
        this.repository = repository;
        this.matchRepository = matchRepository;
        this.qpidClient = qpidClient;
    }

    public ServiceProvider syncSubscriptions(String brokerExternalName, String messageChannelPort, ServiceProvider serviceProvider, QpidDelta delta) {
        if (!serviceProvider.getSubscriptions().isEmpty()) {
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    processSubscription(serviceProvider, subscription, brokerExternalName, messageChannelPort, delta);
                } else {
                    processRedirectSubscription(subscription);
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    public void processSubscription(ServiceProvider serviceProvider, LocalSubscription subscription, String nodeName, String messageChannelPort, QpidDelta delta) {
        switch (subscription.getStatus()) {
            case REQUESTED:
                if (subscription.getLocalEndpoints().isEmpty()) {
                    String queueName = "loc-" + UUID.randomUUID().toString();
                    LocalEndpoint endpoint = new LocalEndpoint(queueName, nodeName, Integer.parseInt(messageChannelPort));
                    subscription.getLocalEndpoints().add(endpoint);
                }
                //NOTE fallthrough!
            case CREATED:
                onRequested(serviceProvider.getName(), subscription, delta);
                break;
            case TEAR_DOWN:
                //	Check that the binding exist, if so, delete it
                onTearDown(serviceProvider, subscription, delta);
                break;
            case ILLEGAL:
                // Remove the subscription from the ServiceProvider
                //serviceProvider.removeSubscription(subscription);
                break;
            //needs testing.
            case ERROR:
                subscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
                break;
            default:
                throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    public void processRedirectSubscription(LocalSubscription subscription) {
        if (subscription.getStatus().equals(LocalSubscriptionStatus.REQUESTED)) {
            subscription.setStatus(LocalSubscriptionStatus.CREATED);
        } else if (subscription.getStatus().equals(LocalSubscriptionStatus.CREATED)) {
            //Just skip
        } else if (subscription.getStatus().equals(LocalSubscriptionStatus.TEAR_DOWN)) {
            subscription.getLocalEndpoints().clear();
        } else if (subscription.getStatus().equals(LocalSubscriptionStatus.ILLEGAL)) {
            subscription.getLocalEndpoints().clear();
            subscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
        }else if (subscription.getStatus().equals(LocalSubscriptionStatus.ERROR)){
            subscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
        } else {
            throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    private void onTearDown(ServiceProvider serviceProvider, LocalSubscription subscription, QpidDelta delta) {
        Set<LocalEndpoint> endpointsToRemove = new HashSet<>();
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            Queue queue = delta.findByQueueName(source);
            if (queue != null) {
                qpidClient.removeReadAccess(serviceProvider.getName(), source);
                qpidClient.removeQueue(queue);
                delta.removeQueue(queue);
                logger.info("Removed queue for LocalSubscription {}", subscription);
            }
            endpointsToRemove.add(endpoint);
        }
        if (!endpointsToRemove.isEmpty()) {
            subscription.getLocalEndpoints().removeAll(endpointsToRemove);
        }
        subscription.getConnections().clear();
    }

    private void onRequested(String serviceProviderName, LocalSubscription subscription, QpidDelta delta) {
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            optionallyCreateQueue(source, serviceProviderName, delta);
        }
        subscription.setStatus(LocalSubscriptionStatus.CREATED);
    }

    private void optionallyCreateQueue(String queueName, String serviceProviderName, QpidDelta delta) {
        Queue queue = delta.findByQueueName(queueName);
        if (queue == null) {
            logger.info("Creating queue {}", queueName);
            queue = qpidClient.createQueue(queueName);
            qpidClient.addReadAccess(serviceProviderName, queueName);
            delta.addQueue(queue);
        }
    }

    public ServiceProvider syncLocalSubscriptionsToServiceProviderCapabilities(ServiceProvider serviceProvider, QpidDelta delta, Iterable<ServiceProvider> serviceProviders) {
        if (serviceProvider.hasActiveSubscriptions()) {
            Set<Capability> allCreatedCapabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
            Set<LocalSubscription> activeSubscriptions = serviceProvider.activeSubscriptions();
            for (LocalSubscription subscription : activeSubscriptions) {
                removeUnusedLocalConnectionsFromLocalSubscription(subscription, allCreatedCapabilities);
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(allCreatedCapabilities, subscription.getSelector());
                    for (Capability capability : matchingCapabilities) {
                        for (CapabilityShard shard : capability.getShards()) {
                            if (CapabilityMatcher.matchCapabilityApplicationWithShardToSelector(capability.getApplication(), shard.getShardId(), subscription.getSelector())){
                                Exchange shardExchange = delta.findByExchangeName(shard.getExchangeName());
                                if (shardExchange != null) {
                                    //TODO need a better way of getting the endpoint
                                    Optional<LocalEndpoint> maybeEndpoint = subscription.getLocalEndpoints().stream().findFirst();
                                    if (maybeEndpoint.isPresent()) {
                                        String source = maybeEndpoint.get().getSource();
                                        if (! shardExchange.isBoundTo(source)) {
                                            Binding binding = new Binding(shard.getExchangeName(), source, new Filter(subscription.getSelector()));
                                            qpidClient.addBinding(shard.getExchangeName(), binding);
                                            shardExchange.addBinding(binding);
                                        }
                                        if (! isExistingConnection(subscription, shard)) {
                                            LocalConnection connection = new LocalConnection(shard.getExchangeName(), source);
                                            subscription.addConnection(connection);
                                        }
                                    } else {
                                        logger.warn("Cound not find endpoint for subscription {}", subscription.getId());
                                    }
                                } else {
                                    logger.info("Could not find exchange {} for shard", shard.getExchangeName());
                                }
                            }
                        }
                    }
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    private boolean isExistingConnection(LocalSubscription subscription, CapabilityShard shard) {
        Set<String> existingConnections = subscription.getConnections().stream()
                .map(LocalConnection::getSource)
                .collect(Collectors.toSet());
        return existingConnections.contains(shard.getExchangeName());
    }

    public void removeUnusedLocalConnectionsFromLocalSubscription(LocalSubscription subscription, Set<Capability> capabilities) {
        Set<String> existingConnections = new HashSet<>();
        for (Capability cap : capabilities) {
            existingConnections.addAll(cap.getExchangesFromShards());
        }

        Set<LocalConnection> unwantedConnections = new HashSet<>();
        for (LocalConnection connection : subscription.getConnections()) {
            if (!existingConnections.contains(connection.getSource())) {
                unwantedConnections.add(connection);
            }
        }
        subscription.getConnections().removeAll(unwantedConnections);
    }


    public ServiceProvider removeUnwantedSubscriptions(ServiceProvider serviceProvider) {
        if (!serviceProvider.getSubscriptions().isEmpty()) {
            Set<LocalSubscription> subscriptionsToRemove = new HashSet<>();
            for (LocalSubscription localSubscription : serviceProvider.getSubscriptions()) {
                if (!localSubscription.isSubscriptionWanted()) {
                    List<Match> matches = matchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
                    if (matches.isEmpty() && localSubscription.getLocalEndpoints().isEmpty()) {
                        subscriptionsToRemove.add(localSubscription);
                    }
                }
            }
            serviceProvider.removeSubscriptions(subscriptionsToRemove);
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }


}
