package no.vegvesen.ixn.federation.service.routing.localdelivery;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LocalDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(LocalDeliveryService.class);


    private ServiceProviderRepository serviceProviderRepository;
    private OutgoingMatchRepository outgoingMatchRepository;


    @Autowired
    public LocalDeliveryService(ServiceProviderRepository serviceProviderRepository, OutgoingMatchRepository outgoingMatchRepository) {
       this.serviceProviderRepository = serviceProviderRepository;
       this.outgoingMatchRepository = outgoingMatchRepository;
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
                        }
                    } else {
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
                        delivery.setStatus(LocalDeliveryStatus.CREATED);
                        logger.info("Delivery with id {} is set to status CREATED", delivery.getId());
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

}
