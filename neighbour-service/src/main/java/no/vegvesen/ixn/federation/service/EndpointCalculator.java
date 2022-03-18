package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.Endpoint;

import java.util.HashSet;
import java.util.Set;

public class EndpointCalculator {
    private final Set<Endpoint> ourEndpoints;
    private final Set<Endpoint> neighbourEndpoints;
    private final Set<Endpoint> endpointsToRemove;
    private final Set<Endpoint> newEndpoints;
    private final Set<Endpoint> calculatedEndpoints;


    public EndpointCalculator(Set<Endpoint> ourEndpoints, Set<Endpoint> neighbourEndpoints) {
        this.ourEndpoints = ourEndpoints;
        this.neighbourEndpoints = neighbourEndpoints;
        this.endpointsToRemove = calculateEndpointsToRemove(ourEndpoints,neighbourEndpoints);
        this.newEndpoints = calculateNewEndpoints(ourEndpoints,neighbourEndpoints);
        this.calculatedEndpoints = calculateResultingEndpoints(ourEndpoints);
    }

    public Set<Endpoint> getEndpointsToRemove() {
        return endpointsToRemove;
    }

    public Set<Endpoint> getNewEndpoints() {
        return newEndpoints;
    }

    private Set<Endpoint> calculateEndpointsToRemove(Set<Endpoint> ourEndpoints, Set<Endpoint> neighbourEndpoints) {
        Set<Endpoint> endpointsToRemove = new HashSet<>(ourEndpoints);
        endpointsToRemove.removeAll(neighbourEndpoints);
        return endpointsToRemove;
    }

    private Set<Endpoint> calculateNewEndpoints(Set<Endpoint> ourEndpoints, Set<Endpoint> neighbourEndpoints) {
        Set<Endpoint> endpointsToAdd = new HashSet<>(neighbourEndpoints);
        endpointsToAdd.removeAll(ourEndpoints);
        return endpointsToAdd;
    }

    private Set<Endpoint> calculateResultingEndpoints(Set<Endpoint> ourEndpoints) {
        Set<Endpoint> calculated = new HashSet<>(ourEndpoints);
        calculated.removeAll(getEndpointsToRemove());
        calculated.addAll(getNewEndpoints());
        return calculated;
    }

    public Set<Endpoint> getCalculatedEndpointsSet() {
        return calculatedEndpoints;
    }
}
