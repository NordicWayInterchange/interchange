package no.vegvesen.ixn.docker.keygen.generator;

import java.nio.file.Path;

public class CertChainAndKey {
    private final Path singleCertOnHost;
    private final Path chainCertOnHost;
    private final Path intermediateKeyOnHost;

    public CertChainAndKey(Path singleCertOnHost, Path chainCertOnHost, Path intermediateKeyOnHost) {

        this.singleCertOnHost = singleCertOnHost;
        this.chainCertOnHost = chainCertOnHost;
        this.intermediateKeyOnHost = intermediateKeyOnHost;
    }

    public Path getSingleCertOnHost() {
        return singleCertOnHost;
    }

    public Path getChainCertOnHost() {
        return chainCertOnHost;
    }

    public Path getIntermediateKeyOnHost() {
        return intermediateKeyOnHost;
    }
}
