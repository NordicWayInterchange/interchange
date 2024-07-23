package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OutgoingMatchDiscoveryService {

    private OutgoingMatchRepository repository;

    private Logger logger = LoggerFactory.getLogger(OutgoingMatchDiscoveryService.class);

    @Autowired
    public OutgoingMatchDiscoveryService(OutgoingMatchRepository repository) {
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
                            Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelectorWithShards(serviceProvider.getCapabilities().getCapabilities(), delivery.getSelector());
                            for (Capability capability : matchingCapabilities) {
                                if (repository.findByCapability_IdAndLocalDelivery_Id(capability.getId(), delivery.getId()) == null) {
                                    if (capability.getStatus().equals(CapabilityStatus.CREATED)) {
                                        OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, capability, serviceProvider.getName());
                                        logger.info("Match saved for delivery with id {}", delivery.getId());
                                        repository.save(outgoingMatch);
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
        List<OutgoingMatch> existingMatches = repository.findAll();
        Set<OutgoingMatch> matchesToDelete = existingMatches.stream().filter(this::capabilityOrDeliveryIsTearDown).collect(Collectors.toSet());
        repository.deleteAll(matchesToDelete);
    }

    public boolean capabilityOrDeliveryIsTearDown(OutgoingMatch match) {
        if (match.capabilityIsTearDown()) {
            return true;
        } else {
            return match.deliveryIsTearDown();
        }
    }
}
