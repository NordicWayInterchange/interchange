package no.vegvesen.ixn.keys.generator;

import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CertificateCertificateChainAndKeys;

public record HostResponse(String host, CertificateCertificateChainAndKeys keyDetails) { }
