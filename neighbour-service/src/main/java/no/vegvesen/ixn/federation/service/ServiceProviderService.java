package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan
public class ServiceProviderService {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderService.class);

    private ServiceProviderRepository serviceProviderRepository;
    private OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;
    private MatchRepository matchRepository;

    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository, OutgoingMatchDiscoveryService outgoingMatchDiscoveryService, MatchRepository matchRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.outgoingMatchDiscoveryService = outgoingMatchDiscoveryService;
        this.matchRepository = matchRepository;
    }

    public void syncServiceProviders(String host, Integer port) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            updateLocalSubscriptionWithRedirectEndpoints(name);
            updateDeliveryStatus(name);
            updateNewLocalDeliveryEndpoints(name, host, port);
            updateTearDownLocalDeliveryEndpoints(name);
            removeTearDownCapabilities(name);
            removeTearDownAndIllegalDeliveries(name);
        }
    }

    public void updateLocalSubscriptionWithRedirectEndpoints(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalSubscription> redirectSubscriptions = serviceProvider.getSubscriptions().stream()
                .filter(l -> l.getConsumerCommonName().equals(serviceProvider.getName()))
                .collect(Collectors.toSet());

        for (LocalSubscription localSubscription : redirectSubscriptions) {
            Set<LocalEndpoint> newEndpoints = new HashSet<>();
            if (localSubscription.getStatus().equals(LocalSubscriptionStatus.CREATED)) {
                List<Match> matches = matchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
                for (Match match : matches) {
                    Set<LocalEndpoint> endpoints = transformEndpointsToLocalEndpoints(match.getSubscription().getEndpoints());
                    if (!localSubscription.getLocalEndpoints().equals(endpoints)) {
                        for (LocalEndpoint endpoint : endpoints) {
                            if (!localSubscription.getLocalEndpoints().contains(endpoint)) {
                                newEndpoints.add(endpoint);
                            }
                        }
                    }
                }
            }
            localSubscription.setLocalEndpoints(newEndpoints);
        }
        serviceProviderRepository.save(serviceProvider);
    }

    public void updateNewLocalDeliveryEndpoints(String serviceProviderName, String host, Integer port) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (!serviceProvider.getDeliveries().isEmpty()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (!delivery.getStatus().equals(LocalDeliveryStatus.ILLEGAL)) {
                    Set<String> targets = new HashSet<>();
                    for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                        String target = endpoint.getTarget();
                        if (target != null) {
                            targets.add(target);
                        } else {
                            logger.debug("Delivery '{}' does not have a target", delivery);
                        }
                    }

                    if (delivery.exchangeExists()) {
                        if (!targets.contains(delivery.getExchangeName())) {
                            LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(host, port, delivery.getExchangeName());
                            delivery.addEndpoint(endpoint);
                        }
                    }
                }
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void updateDeliveryStatus(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (!serviceProvider.getDeliveries().isEmpty()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)
                        || delivery.getStatus().equals(LocalDeliveryStatus.CREATED)
                        || delivery.getStatus().equals(LocalDeliveryStatus.NO_OVERLAP)) {
                    if (outgoingMatchDiscoveryService.findMatchesFromDeliveryId(delivery.getId()).isEmpty()) {
                        delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                    } else {
                        if (!delivery.exchangeExists()) {
                            String deliveryExchangeName = "del-" + UUID.randomUUID().toString();
                            delivery.setExchangeName(deliveryExchangeName);
                        }
                        delivery.setStatus(LocalDeliveryStatus.CREATED);
                        logger.info("Delivery with id {} is set to status CREATED", delivery.getId());
                    }
                }
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void updateTearDownLocalDeliveryEndpoints(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (!serviceProvider.getDeliveries().isEmpty()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                Set<LocalDeliveryEndpoint> endpointsToRemove = new HashSet<>();
                for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                    List<OutgoingMatch> matches = outgoingMatchDiscoveryService.findByDeliveryQueueName(endpoint.getTarget());
                    if (matches.isEmpty()) {
                        endpointsToRemove.add(endpoint);
                    }
                }
                delivery.removeAllEndpoints(endpointsToRemove);
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void removeTearDownCapabilities(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        Set<Capability> capabilitiesToTearDown = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(c -> c.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .collect(Collectors.toSet());

        Capabilities currentServiceProviderCapabilities = serviceProvider.getCapabilities();
        for (Capability capability : capabilitiesToTearDown) {
            List<OutgoingMatch> possibleMatches = outgoingMatchDiscoveryService.findMatchesFromCapabilityId(capability.getId());
            if (possibleMatches.isEmpty()) {
                if (!capability.exchangeExists()) {
                    logger.info("Removing capability with id {} and status TEAR_DOWN", capability.getId());
                    serviceProvider.getCapabilities().getCapabilities().remove(capability);
                    currentServiceProviderCapabilities.setLastUpdated(LocalDateTime.now());
                }
            }
        }

        if (currentServiceProviderCapabilities.getCapabilities().size() == 0) {
            currentServiceProviderCapabilities.setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
        } else {
            currentServiceProviderCapabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
        }
        serviceProviderRepository.save(serviceProvider);
    }

    public void removeTearDownAndIllegalDeliveries(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalDelivery> deliveriesToTearDown = serviceProvider.getDeliveries().stream()
                .filter(d -> d.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN)
                || d.getStatus().equals(LocalDeliveryStatus.ILLEGAL))
                .collect(Collectors.toSet());

        for (LocalDelivery delivery : deliveriesToTearDown) {
            List<OutgoingMatch> possibleMatches = outgoingMatchDiscoveryService.findMatchesFromDeliveryId(delivery.getId());
            if (possibleMatches.isEmpty()) {
                logger.info("Removing delivery with id {}", delivery.getId());
                serviceProvider.getDeliveries().remove(delivery);
            }
        }
        serviceProviderRepository.save(serviceProvider);
    }

    public List<ServiceProvider> getServiceProviders() {
        return serviceProviderRepository.findAll();
    }

    public void saveAllServiceProviders(List<ServiceProvider> serviceProviders) {
        serviceProviderRepository.saveAll(serviceProviders);
    }

    public Set<LocalEndpoint> transformEndpointsToLocalEndpoints(Set<Endpoint> endpoints) {
        Set<LocalEndpoint> localEndpoints = new HashSet<>();
        for (Endpoint endpoint : endpoints) {
            LocalEndpoint localEndpoint = new LocalEndpoint(
                    endpoint.getSource(),
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate());
            localEndpoints.add(localEndpoint);
        }
        return localEndpoints;
    }

}
