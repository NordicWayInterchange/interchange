package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.repository.SubscriptionCapabilityMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubscriptionCapabilityMatchDiscoveryService {

    private final SubscriptionCapabilityMatchRepository subscriptionCapabilityMatchRepository;
    private Logger logger = LoggerFactory.getLogger(SubscriptionCapabilityMatchDiscoveryService.class);


    @Autowired
    public SubscriptionCapabilityMatchDiscoveryService(SubscriptionCapabilityMatchRepository subscriptionCapabilityMatchRepository){
        this.subscriptionCapabilityMatchRepository = subscriptionCapabilityMatchRepository;
    }

    public void createMatch(NeighbourSubscription neighbourSubscription, Capability capability){
        SubscriptionCapabilityMatch subscriptionMatch = subscriptionCapabilityMatchRepository.findSubscriptionCapabilityMatchByCapabilityAndNeighbourSubscription(capability, neighbourSubscription);
        if(subscriptionMatch == null) {
            SubscriptionCapabilityMatch match = new SubscriptionCapabilityMatch(neighbourSubscription, capability);
            subscriptionCapabilityMatchRepository.save(match);
            logger.info("Saved new match {}", match);
        }
    }

    public void deleteMatches(NeighbourSubscription neighbourSubscription){
        List<SubscriptionCapabilityMatch> matches = subscriptionCapabilityMatchRepository.findAllByNeighbourSubscriptionId(neighbourSubscription.getId());
        logger.info("Removing matches {}", matches);
        subscriptionCapabilityMatchRepository.deleteAll(matches);
    }

    public boolean newMatchExists(NeighbourSubscription neighbourSubscription, Set<Capability> capabilities){
        List<SubscriptionCapabilityMatch> matches = subscriptionCapabilityMatchRepository.findAllByNeighbourSubscriptionId(neighbourSubscription.getId());
        if(!matches.isEmpty()) {
            Set<Capability> existingMatches = matches.stream().map(SubscriptionCapabilityMatch::getCapability).collect(Collectors.toSet());
            Set<Capability> newCapabilities = capabilities.stream().filter(a->!existingMatches.contains(a)).collect(Collectors.toSet());
            List<Capability> newMatches = CapabilityMatcher.matchCapabilitiesToSelector(newCapabilities, neighbourSubscription.getSelector()).stream().toList();
            return !newMatches.isEmpty();
        }
        else{
            return false;
        }
    }
}
