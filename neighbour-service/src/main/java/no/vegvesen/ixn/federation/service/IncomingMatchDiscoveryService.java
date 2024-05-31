package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.repository.IncomingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IncomingMatchDiscoveryService {

    private final IncomingMatchRepository incomingMatchRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    @Autowired
    public IncomingMatchDiscoveryService(IncomingMatchRepository incomingMatchRepository, ServiceProviderRepository serviceProviderRepository){
        this.incomingMatchRepository = incomingMatchRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public void createMatches(NeighbourSubscription neighbourSubscription, Set<Capability> matchingCaps){
        for(Capability capability : matchingCaps){
            IncomingMatch match = new IncomingMatch(neighbourSubscription, capability);
            incomingMatchRepository.save(match);
        }
    }

    public void deleteMatches(NeighbourSubscription neighbourSubscription){
        List<IncomingMatch> matches = incomingMatchRepository.findAllByNeighbourSubscriptionId(neighbourSubscription.getId());
        incomingMatchRepository.deleteAll(matches);
    }

    public boolean newMatchExists(NeighbourSubscription neighbourSubscription){

        List<IncomingMatch> matches = incomingMatchRepository.findAllByNeighbourSubscriptionId(neighbourSubscription.getId());

        if(!matches.isEmpty()) {
            Set<Capability> allCapabilities = serviceProviderRepository.findAll().stream().flatMap(a -> a.getCapabilities().getCapabilities().stream()).collect(Collectors.toSet());
            Set<Capability> existingMatches = matches.stream().map(IncomingMatch::getCapability).collect(Collectors.toSet());
            List<Capability> allMatches = CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, neighbourSubscription.getSelector()).stream().toList();
            return !(new HashSet<>(existingMatches).containsAll(allMatches));
        }
        else{
            return false;
        }
    }
}
