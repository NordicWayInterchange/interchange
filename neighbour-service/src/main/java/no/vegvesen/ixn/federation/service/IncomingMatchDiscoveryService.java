package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.repository.IncomingMatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IncomingMatchDiscoveryService {

    private IncomingMatchRepository incomingMatchRepository;

    private ServiceProviderRepository serviceProviderRepository;

    private NeighbourRepository neighbourRepository;
    @Autowired
    public IncomingMatchDiscoveryService(IncomingMatchRepository incomingMatchRepository, ServiceProviderRepository serviceProviderRepository, NeighbourRepository neighbourRepository){
        this.incomingMatchRepository = incomingMatchRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.neighbourRepository = neighbourRepository;
    }

    public void syncNeighbourSubscriptionsAndCapabilitiesToCreateMatch(List<Neighbour> neighbours){
        List<Capability> capabilities = serviceProviderRepository.findAll().stream().flatMap(a->a.getCapabilities().getCapabilities().stream()).toList();
        for(Neighbour neighbour : neighbours){
            for(NeighbourSubscription neighbourSubscription : neighbour.getNeighbourRequestedSubscriptions().getSubscriptions()){
                IncomingMatch match = incomingMatchRepository.findByNeighbourSubscriptionId(neighbourSubscription.getId());
                if(match == null){
                    IncomingMatch incomingMatch = new IncomingMatch();
                    incomingMatch.setNeighbourSubscription(neighbourSubscription);
                    for(Capability capability : capabilities){
                        if(CapabilityMatcher.matchCapabilityToSelector(capability, neighbourSubscription.getSelector())){
                            incomingMatch.addCapability(capability);
                        }
                    }
                    incomingMatchRepository.save(incomingMatch);
                }
                else{
                    Set<Capability> existingCapabilities = match.getCapabilities();
                    Set<Capability> allCapabilities = new HashSet<>();
                    for(Capability capability : capabilities){
                        if(CapabilityMatcher.matchCapabilityToSelector(capability, match.getNeighbourSubscription().getSelector())){
                            allCapabilities.add(capability);
                        }
                    }
                    if(!existingCapabilities.equals(allCapabilities)){
                        neighbourSubscription.setSubscriptionStatus(NeighbourSubscriptionStatus.RESUBSCRIBE);
                        neighbourRepository.save(neighbour);
                    }
                }
            }
        }
    }
    public void syncMatchesToDelete(){
        List<IncomingMatch> incomingMatches = incomingMatchRepository.findAll();
        for(IncomingMatch incomingMatch : incomingMatches){
            NeighbourSubscriptionStatus status = incomingMatch.getNeighbourSubscription().getSubscriptionStatus();
            if(status.equals(NeighbourSubscriptionStatus.TEAR_DOWN) || status.equals(NeighbourSubscriptionStatus.RESUBSCRIBE)){
                incomingMatchRepository.delete(incomingMatch);
            }
        }
    }
}
