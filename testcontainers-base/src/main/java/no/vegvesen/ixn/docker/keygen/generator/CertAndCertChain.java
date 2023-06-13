package no.vegvesen.ixn.docker.keygen.generator;

import java.nio.file.Path;

public class CertAndCertChain {


    private final Path singleCertOnHost;
    private final Path certChainOnHost;

    public CertAndCertChain(Path singleCertOnHost, Path certChainOnHost) {
        this.singleCertOnHost = singleCertOnHost;
        this.certChainOnHost = certChainOnHost;
    }

    public Path getSingleCertOnHost() {
        return singleCertOnHost;
    }

    public Path getCertChainOnHost() {
        return certChainOnHost;
    }
}
