package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OutgoingMatchDiscoveryService {

    private OutgoingMatchRepository repository;

    private Logger logger = LoggerFactory.getLogger(OutgoingMatchDiscoveryService.class);

    @Autowired
    public OutgoingMatchDiscoveryService(OutgoingMatchRepository repository) {
        this.repository = repository;
    }

    public List<ServiceProvider> syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(List<ServiceProvider> serviceProviders) {
        List<ServiceProvider> serviceProvidersToSave = new ArrayList<>();
        for (ServiceProvider serviceProvider: serviceProviders) {
            if (serviceProvider.hasDeliveries()) {
                for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                    if (serviceProvider.hasCapabilities()) {
                        if (delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)
                                || delivery.getStatus().equals(LocalDeliveryStatus.CREATED)
                                || delivery.getStatus().equals(LocalDeliveryStatus.NO_OVERLAP)) {
                            boolean isValid = false;
                            for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
                                if (repository.findByCapability_IdAndLocalDelivery_Id(capability.getId(), delivery.getId()) != null) {
                                    isValid = true;
                                } else {
                                    if (capability.getStatus().equals(CapabilityStatus.CREATED)) {
                                        boolean match = CapabilityMatcher.matchCapabilityToSelector(capability, delivery.getSelector());
                                        if (match) {
                                            isValid = true;
                                            if (!delivery.exchangeExists()) {
                                                String deliveryExchangeName = "del-" + UUID.randomUUID().toString();
                                                delivery.setExchangeName(deliveryExchangeName);
                                            }
                                            OutgoingMatch outgoingMatch = new OutgoingMatch(delivery, capability, serviceProvider.getName(), OutgoingMatchStatus.SETUP_ENDPOINT);
                                            logger.info("Delivery with id {} saved with status CREATED", delivery.getId());
                                            delivery.setStatus(LocalDeliveryStatus.CREATED);
                                            repository.save(outgoingMatch);
                                        }
                                    }
                                }
                            }
                            if (!isValid) {
                                delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                                logger.info("Delivery with selector {} does not match any service provider capability", delivery.getSelector());
                            }
                        }
                    } else {
                        if (!delivery.getStatus().equals(LocalDeliveryStatus.ILLEGAL)) {
                            delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                            logger.info("Delivery with selector {} does not match any service provider capability", delivery.getSelector());
                        }
                    }
                }
            }
            serviceProvidersToSave.add(serviceProvider);
        }
        return serviceProvidersToSave;
    }

    public List<OutgoingMatch> findMatchesToSetupEndpointFor(String serviceProviderName) {
        return repository.findAllByServiceProviderNameAndStatus(serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);
    }

    public List<OutgoingMatch> findMatchesToTearDownEndpointsFor(String serviceProviderName) {
        return repository.findAllByServiceProviderNameAndStatus(serviceProviderName, OutgoingMatchStatus.TEARDOWN_ENDPOINT);
    }

    public List<OutgoingMatch> findMatchesWithNewEndpoints(String serviceProviderName, Integer deliveryId) {
        return repository.findAllByServiceProviderNameAndLocalDelivery_IdAndStatus(serviceProviderName, deliveryId, OutgoingMatchStatus.UP);
    }

    public OutgoingMatch findMatchFromDeliveryId(Integer deliveryId) {
        return repository.findByLocalDelivery_Id(deliveryId);
    }

    public List<OutgoingMatch> findMatchesFromDeliveryId(Integer deliveryId) {
        return repository.findAllByLocalDelivery_Id(deliveryId);
    }

    public List<OutgoingMatch> findByDeliveryQueueName(String deliveryExchangeName) {
        return repository.findAllByLocalDelivery_ExchangeName(deliveryExchangeName);
    }

    public List<OutgoingMatch> findMatchesFromCapabilityId(Integer capabilityId) {
        return repository.findAllByCapability_Id(capabilityId);
    }

    public void updateOutgoingMatchToUp(OutgoingMatch match) {
        match.setStatus(OutgoingMatchStatus.UP);
        logger.info("Saving match {} with status UP", match);
        repository.save(match);
    }

    public void syncLocalSubscriptionAndSubscriptionsToTearDownOutgoingMatchResources() {
        List<OutgoingMatch> matches = repository.findAllByStatus(OutgoingMatchStatus.UP);
        for (OutgoingMatch match : matches) {
            if(match.getCapability().getStatus().equals(CapabilityStatus.TEAR_DOWN) ||
                match.getLocalDelivery().getStatus().equals(LocalDeliveryStatus.TEAR_DOWN)) {
                match.setStatus(OutgoingMatchStatus.TEARDOWN_ENDPOINT);
                repository.save(match);
                logger.info("Saved match {} with status TEARDOWN_ENDPOINT", match);
            }
        }
    }

    public void updateOutgoingMatchToDeleted(OutgoingMatch match) {
        match.setStatus(OutgoingMatchStatus.DELETED);
        repository.save(match);
        logger.info("Saved match {} with status DELETED", match);
    }

    public void removeMatchesThatAreDeleted() {
        List<OutgoingMatch> matchesToRemove = repository.findAllByStatus(OutgoingMatchStatus.DELETED);
        if (!matchesToRemove.isEmpty()) {
            repository.deleteAll(matchesToRemove);
            logger.info("Removed deleted OutgoingMatches");
        }
    }

    public OutgoingMatch findByOutgoingMatchId(Integer id) {
        return repository.findById(id).get();
    }
}
