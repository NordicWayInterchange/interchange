package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;

import java.util.HashSet;
import java.util.Set;

public class CapabilityTransformer {
    public Set<CapabilitySplit> transformNeighbourCapabilityToSplitCapability(Set<NeighbourCapability> capabilities){
        Set<CapabilitySplit> capabilitySplits = new HashSet<>();
        for(NeighbourCapability i : capabilities){
            capabilitySplits.add(
                    new CapabilitySplit(i.getApplication(), i.getMetadata())
            );

        }
        return capabilitySplits;
    }
}
