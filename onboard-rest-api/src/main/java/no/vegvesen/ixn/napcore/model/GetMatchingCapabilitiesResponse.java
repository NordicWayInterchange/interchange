package no.vegvesen.ixn.napcore.model;

import java.util.HashSet;

public class GetMatchingCapabilitiesResponse {

    HashSet<Capability> capabilities;

    public GetMatchingCapabilitiesResponse() {

    }

    public GetMatchingCapabilitiesResponse(HashSet<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public HashSet<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(HashSet<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public String toString() {
        return "MatchingCapabilitiesResponse{" +
                "capabilities=" + capabilities +
                '}';
    }
}
