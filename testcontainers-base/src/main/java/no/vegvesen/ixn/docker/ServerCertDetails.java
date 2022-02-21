package no.vegvesen.ixn.docker;

import java.nio.file.Path;

class ServerCertDetails {
    private final Path keyOnHost;
    private final Path certOnHost;
    private final Path certChainOnHost;

    public ServerCertDetails(Path keyOnHost, Path certOnHost, Path certChainOnHost) {
        this.keyOnHost = keyOnHost;
        this.certOnHost = certOnHost;
        this.certChainOnHost = certChainOnHost;
    }

    public Path getKeyOnHost() {
        return keyOnHost;
    }

    public Path getCertOnHost() {
        return certOnHost;
    }

    public Path getCertChainOnHost() {
        return certChainOnHost;
    }
}
