package no.vegvesen.ixn.docker;

import java.nio.file.Path;

class ServiceProviderCertDetails {
    private final Path certOnHost;
    private final Path certChainOnHost;

    public ServiceProviderCertDetails(Path certOnHost, Path certChainOnHost) {
        this.certOnHost = certOnHost;
        this.certChainOnHost = certChainOnHost;
    }

    public Path getCertOnHost() {
        return certOnHost;
    }

    public Path getCertChainOnHost() {
        return certChainOnHost;
    }
}
