package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.routing.localdelivery.LocalDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan
public class ServiceProviderService {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderService.class);

    private ServiceProviderRepository serviceProviderRepository;
    private OutgoingMatchRepository outgoingMatchRepository;
    private MatchRepository matchRepository;
    private LocalDeliveryService localDeliveryService;

    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository, OutgoingMatchRepository outgoingMatchRepository, MatchRepository matchRepository, LocalDeliveryService localDeliveryService) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.outgoingMatchRepository = outgoingMatchRepository;
        this.matchRepository = matchRepository;
        this.localDeliveryService = localDeliveryService;
    }

    public void syncServiceProviders(String host, Integer port) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            updateLocalSubscriptionWithRedirectEndpoints(name);
            localDeliveryService.updateDeliveryStatus(name, host, port);
            removeTearDownCapabilities(name);
            localDeliveryService.removeTearDownIllegalAndErrorDeliveries(name);
        }
    }

    public void updateLocalSubscriptionWithRedirectEndpoints(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        Set<LocalSubscription> redirectSubscriptions = serviceProvider.getSubscriptions().stream()
                .filter(l -> l.getConsumerCommonName().equals(serviceProvider.getName()))
                .collect(Collectors.toSet());

        for (LocalSubscription localSubscription : redirectSubscriptions) {
            Set<LocalEndpoint> newEndpoints = new HashSet<>();
            Set<LocalEndpoint> endpointsToRemove = new HashSet<>();
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

                        for (LocalEndpoint endpoint : localSubscription.getLocalEndpoints()) {
                            if (endpoints.contains(endpoint)) {
                                endpointsToRemove.add(endpoint);
                            }
                        }
                    }
                }
                if (matches.isEmpty()) {
                    localSubscription.getLocalEndpoints().clear();
                }
            }
            localSubscription.getLocalEndpoints().removeAll(endpointsToRemove);
            localSubscription.getLocalEndpoints().addAll(newEndpoints);

        }
        serviceProviderRepository.save(serviceProvider);
    }



    public void removeTearDownCapabilities(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);

        List<Capability> capabilitiesWithStatusTearDown = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(c -> c.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .toList();

        Capabilities currentServiceProviderCapabilities = serviceProvider.getCapabilities();
        HashSet<Capability> capabilitiesToRemove = new HashSet<>();
        for (Capability capability : capabilitiesWithStatusTearDown) {
            List<OutgoingMatch> possibleMatches = outgoingMatchRepository.findAllByCapability_Id(capability.getId());
            if (possibleMatches.isEmpty()) {
                if (!capability.hasShards()) {
                    logger.info("Removing capability with id {} and status TEAR_DOWN", capability.getId());
                    capabilitiesToRemove.add(capability);
                }
            }
        }

        currentServiceProviderCapabilities.removeCapabilities(capabilitiesToRemove);
        serviceProviderRepository.save(serviceProvider);
    }


    public List<ServiceProvider> getServiceProviders() {
        return serviceProviderRepository.findAll();
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
