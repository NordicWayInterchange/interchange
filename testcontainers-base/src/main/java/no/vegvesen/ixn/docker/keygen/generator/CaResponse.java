package no.vegvesen.ixn.docker.keygen.generator;

import java.util.List;

public record CaResponse(ClusterKeyGenerator.CertificateCertificateChainAndKeys details, String name,List<HostResponse> hostResponses,
                         List<ClientResponse> clientResponses,List<CaResponse> caResponses) {
}
