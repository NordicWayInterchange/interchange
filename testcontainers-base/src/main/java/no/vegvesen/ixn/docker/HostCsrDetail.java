package no.vegvesen.ixn.docker;

import java.nio.file.Path;

class HostCsrDetail {
    private final Path csrOnHost;
    private final Path keyOnHost;

    public HostCsrDetail(Path csrOnHost, Path keyOnHost) {
        this.csrOnHost = csrOnHost;
        this.keyOnHost = keyOnHost;
    }

    public Path getCsrOnHost() {
        return csrOnHost;
    }

    public Path getKeyOnHost() {
        return keyOnHost;
    }
}
