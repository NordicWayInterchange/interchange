package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConfigurationPropertiesScan
public class ServiceProviderService {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderService.class);

    private NeighbourRepository neighbourRepository;
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    public ServiceProviderService(NeighbourRepository neighbourRepository,
                                  ServiceProviderRepository serviceProviderRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public List<ServiceProvider> findAllServiceProviders() {
        return serviceProviderRepository.findAll();
    }

    public void updateLocalDeliveries(String host, String port) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for(ServiceProvider serviceProvider : serviceProviders) {
            if (serviceProvider.hasCapabilities() && !serviceProvider.getDeliveries().isEmpty()) {
                for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                    if (delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)) {
                        boolean match = CapabilityMatcher.matchLocalDeliveryToServiceProviderCapabilities(serviceProvider.getCapabilities().getCapabilities(), delivery);
                        if (match) {
                            LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(
                                    host,
                                    Integer.parseInt(port),
                                    "onramp",
                                    delivery.getSelector()
                            );
                            delivery.setEndpoints(Collections.singleton(endpoint));
                            delivery.setStatus(LocalDeliveryStatus.CREATED);
                        }
                    }
                }
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public List<ServiceProvider> getServiceProviders() {
        return serviceProviderRepository.findAll();
    }

}
