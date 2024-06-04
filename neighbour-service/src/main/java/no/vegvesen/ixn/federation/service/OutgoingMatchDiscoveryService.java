package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.repository.DeliveryCapabilityMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class OutgoingMatchDiscoveryService {

    private DeliveryCapabilityMatchRepository repository;

    private Logger logger = LoggerFactory.getLogger(OutgoingMatchDiscoveryService.class);

    @Autowired
    public OutgoingMatchDiscoveryService(DeliveryCapabilityMatchRepository repository) {
        this.repository = repository;
    }

    public void syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(List<ServiceProvider> serviceProviders) {
        for (ServiceProvider serviceProvider: serviceProviders) {
            if (serviceProvider.hasDeliveries()) {
                for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                    if (serviceProvider.hasCapabilities()) {
                        if (delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)
                                || delivery.getStatus().equals(LocalDeliveryStatus.CREATED)
                                || delivery.getStatus().equals(LocalDeliveryStatus.NO_OVERLAP)) {
                            Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(serviceProvider.getCapabilities().getCapabilities(), delivery.getSelector());
                            for (Capability capability : matchingCapabilities) {
                                if (repository.findByCapability_IdAndLocalDelivery_Id(capability.getId(), delivery.getId()) == null) {
                                    if (capability.getStatus().equals(CapabilityStatus.CREATED)) {
                                        DeliveryCapabilityMatch deliveryCapabilityMatch = new DeliveryCapabilityMatch(delivery, capability, serviceProvider.getName());
                                        logger.info("SubscriptionMatch saved for delivery with id {}", delivery.getId());
                                        repository.save(deliveryCapabilityMatch);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void syncOutgoingMatchesToDelete() {
        List<DeliveryCapabilityMatch> existingMatches = repository.findAll();
        Set<DeliveryCapabilityMatch> matchesToDelete = new HashSet<>();
        for (DeliveryCapabilityMatch match : existingMatches) {
            if (match.capabilityIsTearDown()) {
                logger.info("Removing DeliveryCapabilityMatch {}", match);
                matchesToDelete.add(match);
            } else if (match.deliveryIsTearDown()) {
                logger.info("Removing DeliveryCapabilityMatch {}", match);
                matchesToDelete.add(match);
            }
        }
        repository.deleteAll(matchesToDelete);
    }
}
