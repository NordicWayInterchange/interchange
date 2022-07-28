package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
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

    @Autowired
    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository, OutgoingMatchDiscoveryService outgoingMatchDiscoveryService) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.outgoingMatchDiscoveryService = outgoingMatchDiscoveryService;
    }

    public void syncServiceProviders(String host, Integer port) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider serviceProvider : serviceProviders) {
            updateNewLocalDeliveryEndpoints(serviceProvider, host, port);
            updateTearDownLocalDeliveryEndpoints(serviceProvider);
            removeTearDownCapabilities(serviceProvider);
            removeTearDownDeliveries(serviceProvider);
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void updateNewLocalDeliveryEndpoints(ServiceProvider serviceProvider, String host, Integer port) {
        if (serviceProvider.hasDeliveries()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                Set<String> targets = delivery.getEndpoints().stream()
                        .map(LocalDeliveryEndpoint::getTarget)
                        .collect(Collectors.toSet());
                if (delivery.exchangeExists()) {
                    if (!targets.contains(delivery.getExchangeName())) {
                        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(host, port, delivery.getExchangeName(), delivery.getSelector());
                        delivery.addEndpoint(endpoint);
                    }
                }
            }
        }
    }

    public void updateTearDownLocalDeliveryEndpoints(ServiceProvider serviceProvider) {
        if (serviceProvider.hasDeliveries()) {
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
        }
    }

    public void removeTearDownCapabilities(ServiceProvider serviceProvider) {
        Set<Capability> capabilitiesToTearDown = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(c -> c.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .collect(Collectors.toSet());

        for (Capability capability : capabilitiesToTearDown) {
            List<OutgoingMatch> possibleMatches = outgoingMatchDiscoveryService.findMatchesFromCapabilityId(capability.getId());
            if (possibleMatches.isEmpty()) {
                if (!capability.exchangeExists()) {
                    logger.info("Removing capability with id {} and status TEAR_DOWN", capability.getId());
                    serviceProvider.getCapabilities().getCapabilities().remove(capability);
                }
            }
        }

        Capabilities currentServiceProviderCapabilities = serviceProvider.getCapabilities();
        if (currentServiceProviderCapabilities.getCapabilities().size() == 0) {
            currentServiceProviderCapabilities.setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
        } else {
            currentServiceProviderCapabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
        }
        currentServiceProviderCapabilities.setLastUpdated(LocalDateTime.now());
    }

    public void removeTearDownDeliveries(ServiceProvider serviceProvider) {
        Set<LocalDelivery> deliveriesToTearDown = serviceProvider.getDeliveries().stream()
                .filter(d -> d.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN))
                .collect(Collectors.toSet());

        for (LocalDelivery delivery : deliveriesToTearDown) {
            List<OutgoingMatch> possibleMatches = outgoingMatchDiscoveryService.findMatchesFromDeliveryId(delivery.getId());
            if (possibleMatches.isEmpty()) {
                logger.info("Removing delivery with id {} and status TEAR_DOWN", delivery.getId());
                serviceProvider.getDeliveries().remove(delivery);
            }
        }
    }

    public List<ServiceProvider> getServiceProviders() {
        return serviceProviderRepository.findAll();
    }

    public void saveAllServiceProviders(List<ServiceProvider> serviceProviders) {
        serviceProviderRepository.saveAll(serviceProviders);
    }

}
