package no.vegvesen.ixn.docker.keygen.generator;

import java.nio.file.Path;
import java.util.List;

public class CertAndCertChain {


    private final Path singleCertOnHost;
    private final Path certChainOnHost;

    private List<String> certChainAsString;

    public CertAndCertChain(Path singleCertOnHost, Path certChainOnHost) {
        this.singleCertOnHost = singleCertOnHost;
        this.certChainOnHost = certChainOnHost;
    }

    public CertAndCertChain(Path singleCertOnHost, Path certChainOnHost, List<String> certChainAsString) {

        this.singleCertOnHost = singleCertOnHost;
        this.certChainOnHost = certChainOnHost;
        this.certChainAsString = certChainAsString;
    }

    public Path getSingleCertOnHost() {
        return singleCertOnHost;
    }

    public Path getCertChainOnHost() {
        return certChainOnHost;
    }

    public List<String> getCertChainAsString() {
        return certChainAsString;
    }
}
