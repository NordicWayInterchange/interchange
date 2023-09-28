package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                            Set<CapabilitySplit> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(serviceProvider.getCapabilities().getCapabilities(), delivery.getSelector());
                            for (CapabilitySplit capability : matchingCapabilities) {
                                if (repository.findByCapability_IdAndLocalDelivery_Id(capability.getId(), delivery.getId()) == null) {
                                    if (capability.getStatus().equals(CapabilityStatus.CREATED)) {
                                        OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, capability, serviceProvider.getName());
                                        logger.info("Match saved for delivery with id {}", delivery.getId());
                                        repository.save(outgoingMatch);
                                    }
                                }
                            }
                            //TODO, set to NO_OVERLAP if we cannot find any matching capabilities
                        }
                    }
                }
            }
        }
    }

    public void syncOutgoingMatchesToDelete() {
        List<OutgoingMatch> existingMatches = repository.findAll();
        Set<OutgoingMatch> matchesToDelete = new HashSet<>();
        for (OutgoingMatch match : existingMatches) {
            if (match.capabilityIsTearDown()) {
                logger.info("Removing OutgoingMatch {}", match);
                matchesToDelete.add(match);
            } else if (match.deliveryIsTearDown()) {
                logger.info("Removing OutgoingMatch {}", match);
                matchesToDelete.add(match);
            }
        }
        repository.deleteAll(matchesToDelete);
    }
}
