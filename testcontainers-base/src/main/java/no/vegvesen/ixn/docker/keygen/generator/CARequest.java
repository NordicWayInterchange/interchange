package no.vegvesen.ixn.docker.keygen.generator;

import java.util.List;

public record CARequest(String name, String country, List<CARequest> subCaRequests, List<HostRequest> hostRequests, List<ClientRequest> clientRequests) {
}
