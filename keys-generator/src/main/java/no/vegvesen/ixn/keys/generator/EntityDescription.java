package no.vegvesen.ixn.keys.generator;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

public record EntityDescription(KeyPair keyPair, X509Certificate certificate, List<X509Certificate> certificateChain) {
}
