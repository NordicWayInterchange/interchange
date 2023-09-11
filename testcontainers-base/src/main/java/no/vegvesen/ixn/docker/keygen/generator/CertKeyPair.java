package no.vegvesen.ixn.docker.keygen.generator;

import java.nio.file.Path;

public class CertKeyPair {


    private final Path caCertOnHost;
    private final Path caKeyOnHost;

    public CertKeyPair(Path caCertOnHost, Path caKeyOnHost) {

        this.caCertOnHost = caCertOnHost;
        this.caKeyOnHost = caKeyOnHost;
    }

    public Path getCaCertOnHost() {
        return caCertOnHost;
    }

    public Path getCaKeyOnHost() {
        return caKeyOnHost;
    }
}
