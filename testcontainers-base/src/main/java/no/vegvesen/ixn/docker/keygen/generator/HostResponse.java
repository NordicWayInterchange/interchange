package no.vegvesen.ixn.docker.keygen.generator;

import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.CertificateCertificateChainAndKeys;

public record HostResponse(String host, CertificateCertificateChainAndKeys keyDetails) { }
