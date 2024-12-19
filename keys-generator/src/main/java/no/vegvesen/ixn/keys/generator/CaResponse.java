package no.vegvesen.ixn.keys.generator;

import java.util.List;

public record CaResponse(EntityDescription details, String name, List<HostResponse> hostResponses,
                         List<ClientResponse> clientResponses, List<CaResponse> caResponses) {
}
