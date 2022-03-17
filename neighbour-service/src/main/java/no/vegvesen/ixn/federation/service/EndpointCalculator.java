package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.Endpoint;

import java.util.HashSet;
import java.util.Set;

public class EndpointCalculator {
    private final Set<Endpoint> ourEndpoints;
    private final Set<Endpoint> neighbourEndpoints;

    public EndpointCalculator(Set<Endpoint> ourEndpoints, Set<Endpoint> neighbourEndpoints) {
        this.ourEndpoints = ourEndpoints;
        this.neighbourEndpoints = neighbourEndpoints;
    }

    public Set<Endpoint> getEndpointsToRemove() {
        Set<Endpoint> endpointsToRemove = new HashSet<>(ourEndpoints);
        endpointsToRemove.removeAll(neighbourEndpoints);
        return endpointsToRemove;
    }

    public Set<Endpoint> getNewEndpoints() {
        Set<Endpoint> endpointsToAdd = new HashSet<>(neighbourEndpoints);
        endpointsToAdd.removeAll(ourEndpoints);
        return endpointsToAdd;
    }

    public Set<Endpoint> getCalculatedEndpointsSet() {
        Set<Endpoint> calculated = new HashSet<>(ourEndpoints);
        calculated.removeAll(getEndpointsToRemove());
        calculated.addAll(getNewEndpoints());
        return calculated;
    }
}
