package no.vegvesen.ixn.federation.service.routing.localdelivery;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocalDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(LocalDeliveryService.class);


    private ServiceProviderRepository serviceProviderRepository;
    private OutgoingMatchRepository outgoingMatchRepository;
    private  QpidClient qpidClient;


    @Autowired
    public LocalDeliveryService(ServiceProviderRepository serviceProviderRepository, OutgoingMatchRepository outgoingMatchRepository, QpidClient qpidClient) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.outgoingMatchRepository = outgoingMatchRepository;
        this.qpidClient = qpidClient;
    }

    public void updateDeliveryStatus(String host, Integer port, ServiceProvider serviceProvider) {
        if (!serviceProvider.getDeliveries().isEmpty()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)
                        || delivery.getStatus().equals(LocalDeliveryStatus.CREATED)
                        || delivery.getStatus().equals(LocalDeliveryStatus.NO_OVERLAP)) {
                    if (outgoingMatchRepository.findAllByLocalDelivery_Id(delivery.getId()).isEmpty()) {
                        if (! delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)) {
                            delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                        } else {
                            Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(serviceProvider.getCapabilities().getCapabilities(), delivery.getSelector());
                            if (matchingCapabilities.isEmpty()) {
                                delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                            }
                            //TODO if we find matching capabilities, should they not be created? And should we not create the exchanges?
                        }
                    } else {
                        //TODO this should be done when constructing the objects, surely?
                        if (delivery.getEndpoints().isEmpty()) {
                            String target = "del-" + UUID.randomUUID();
                            if (!delivery.isDlqueue()) {
                                delivery.addEndpoint(new LocalDeliveryEndpoint(
                                        host, port, target
                                ));
                            }
                            if (delivery.isDlqueue()) {
                                String dlqName = "dlq-" + UUID.randomUUID();
                                delivery.addEndpoint(new LocalDeliveryEndpoint(
                                        host, port, target, dlqName
                                ));
                            }
                        }
                        //TODO we set the status to created, without creating exchanges in qpid...
                        delivery.setStatus(LocalDeliveryStatus.CREATED);
                        logger.info("Delivery with id {} is set to status CREATED", delivery.getId());
                    }
                }
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void setUpDeliveryQueue(ServiceProvider serviceProvider, QpidDelta delta) {
        if (serviceProvider.hasDeliveries()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (delivery.getStatus().equals(LocalDeliveryStatus.CREATED)) {
                    List<OutgoingMatch> matches = outgoingMatchRepository.findAllByLocalDelivery_Id(delivery.getId());
                    for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                        String exchangeName = endpoint.getTarget();
                        Exchange exchange = delta.findByExchangeName(exchangeName);
                        if (exchange == null) {
                            if (endpoint.getDlqName() != null) {
                                Queue queue = qpidClient.getQueue(endpoint.getDlqName());
                                if (queue == null) {
                                    Queue createdDlq = qpidClient.createQueue(endpoint.getDlqName());
                                    qpidClient.addReadAccess(serviceProvider.getName(),createdDlq.getName());
                                    delta.addQueue(createdDlq);
                                }
                                exchange = qpidClient.createHeadersExchangeWithDlq(exchangeName, endpoint.getDlqName());
                                logger.info("Created direct exchange {} with dlqueue {}", exchangeName, endpoint.getDlqName());
                            } else {
                                exchange = qpidClient.createHeadersExchange(exchangeName);
                                logger.info("Created exchange {}", exchangeName);
                            }
                            qpidClient.addWriteAccess(serviceProvider.getName(), exchangeName);
                            delta.addExchange(exchange);
                        }
                    }

                    for (OutgoingMatch match : matches) {
                        Capability capability = match.getCapability();
                        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                            for (CapabilityShard shard : capability.getShards()) {
                                Exchange endpointExchange = delta.findByExchangeName(endpoint.getTarget());
                                Exchange shardExchange = delta.findByExchangeName(shard.getExchangeName());

                                //NOTE, there's not much chance of the endpointExchange not existing, since it most likely
                                // is created in the previous loop if it didn't already exist
                                if (endpointExchange != null) {
                                    if (shardExchange != null) {
                                        if (!endpointExchange.isBoundTo(shardExchange.getName())) {
                                            if (CapabilityMatcher.matchCapabilityApplicationWithShardToSelector(capability.getApplication(), shard.getShardId(), delivery.getSelector())) {
                                                String joinedSelector = joinTwoSelectors(shard.getSelector(), delivery.getSelector());
                                                Binding binding = new Binding(endpointExchange.getName(), shardExchange.getName(), new Filter(joinedSelector));
                                                qpidClient.addBinding(endpointExchange.getName(), binding);
                                                endpointExchange.addBinding(binding);
                                                logger.info("Added binding from {} to {}", endpointExchange.getName(), shardExchange.getName());
                                            }
                                        }
                                    } else {
                                        logger.info("No shard exchange found in qpid with name {}",shard.getExchangeName());
                                    }
                                } else {
                                    logger.info("No delivery endpoint exchange found in qpid with name {}",endpoint.getTarget());
                                }
                            }
                        }
                    }
                }
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void removeTearDownIllegalAndErrorDeliveries(ServiceProvider serviceProvider) {
        Set<LocalDelivery> deliveriesToTearDown = serviceProvider.getDeliveries().stream()
                .filter(d -> d.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN)
                        || d.getStatus().equals(LocalDeliveryStatus.ILLEGAL)
                        || d.getStatus().equals(LocalDeliveryStatus.ERROR))
                .collect(Collectors.toSet());

        for (LocalDelivery delivery : deliveriesToTearDown) {
            List<OutgoingMatch> possibleMatches = outgoingMatchRepository.findAllByLocalDelivery_Id(delivery.getId());
            if (possibleMatches.isEmpty() && delivery.getEndpoints().isEmpty()) {
                logger.info("Removing delivery with id {}", delivery.getId());
                serviceProvider.getDeliveries().remove(delivery);
            }
        }
        serviceProviderRepository.save(serviceProvider);
    }

    public String joinTwoSelectors(String firstSelector, String secondSelector) {
        return String.format("(%s) AND (%s)", firstSelector, secondSelector);
    }

    public ServiceProvider tearDownDeliveryQueues(ServiceProvider serviceProvider, QpidDelta delta) {
        if (!serviceProvider.getDeliveries().isEmpty()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (!delivery.getStatus().equals(LocalDeliveryStatus.ILLEGAL)
                        && !delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)) {
                    List<OutgoingMatch> matches = outgoingMatchRepository.findAllByLocalDelivery_Id(delivery.getId());
                    if (matches.isEmpty()) {
                        HashSet<LocalDeliveryEndpoint> endpointsToRemove = new HashSet<>();
                        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                            if (endpoint.targetExists()) {
                                String target = endpoint.getTarget();
                                Exchange exchange = delta.findByExchangeName(target);
                                if (exchange != null) {
                                    logger.info("Removing endpoint with name {} for service provider {}", target, serviceProvider.getName());
                                    qpidClient.removeWriteAccess(serviceProvider.getName(), target);
                                    qpidClient.removeExchange(exchange);
                                    delta.removeExchange(exchange);
                                }
                                endpointsToRemove.add(endpoint);
                            }
                            if (endpoint.getDlqName() != null) {
                                String dlqName = endpoint.getDlqName();
                                Queue dlq = delta.findByQueueName(dlqName);
                                if (dlq != null) {
                                    logger.info("Removing endpoint with dlQueue with name {} for service provider {}", dlqName, serviceProvider.getName());
                                    qpidClient.removeReadAccess(serviceProvider.getName(), dlqName);
                                    qpidClient.removeQueue(dlq);
                                    delta.removeQueue(dlq);
                                }
                            }
                        }
                        delivery.removeAllEndpoints(endpointsToRemove);
                        if (!(delivery.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN) || delivery.getStatus().equals(LocalDeliveryStatus.ERROR))) {
                            delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                        }
                    }
                }
            }
            serviceProvider = serviceProviderRepository.save(serviceProvider);
        }
        return serviceProvider;
    }

}
