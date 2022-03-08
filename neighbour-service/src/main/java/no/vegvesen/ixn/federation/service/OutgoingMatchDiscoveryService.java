package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
            if (serviceProvider.hasCapabilities() && !serviceProvider.getDeliveries().isEmpty()) {
                for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                    if (delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)) {
                        boolean isValid = false;
                        for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
                            boolean match = CapabilityMatcher.matchCapabilityToSelector(capability, delivery.getSelector());
                            if (match) {
                                isValid = true;
                                OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, capability, serviceProvider.getName(), OutgoingMatchStatus.SETUP_ENDPOINT);
                                repository.save(outgoingMatch);
                                delivery.setStatus(LocalDeliveryStatus.CREATED);
                                break;
                            }
                        }
                        if (!isValid) {
                            delivery.setStatus(LocalDeliveryStatus.ILLEGAL);
                            logger.info("Delivery with selector {} does not match any service provider capability", delivery.getSelector());
                        }
                    }
                }
            }
        }
    }

    public List<OutgoingMatch> findMatchesToSetupEndpointFor(String serviceProviderName) {
        return repository.findAllByServiceProviderNameAndStatus(serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);
    }

    public void updateOutgoingMatchToUp(OutgoingMatch match) {
        match.setStatus(OutgoingMatchStatus.UP);
        repository.save(match);
        logger.info("Saved match {} with status UP", match);
    }

}
