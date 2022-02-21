package no.vegvesen.ixn.docker;

import java.nio.file.Path;

class IntermedateCaDetails {
    private final Path singleCertOnHost;
    private final Path chainCertOnHost;

    public IntermedateCaDetails(Path singleCertOnHost, Path chainCertOnHost) {
        this.singleCertOnHost = singleCertOnHost;
        this.chainCertOnHost = chainCertOnHost;
    }

    public Path getSingleCertOnHost() {
        return singleCertOnHost;
    }

    public Path getChainCertOnHost() {
        return chainCertOnHost;
    }
}
