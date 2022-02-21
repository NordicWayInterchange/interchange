package no.vegvesen.ixn.docker;

import java.nio.file.Path;

public class RootCaDetails {

    private final Path caKeyOnHost;
    private final Path caCertOnHost;

    public RootCaDetails(Path caKeyOnHost, Path caCertOnHost) {
        this.caKeyOnHost = caKeyOnHost;
        this.caCertOnHost = caCertOnHost;
    }

    public Path getCaKeyOnHost() {
        return caKeyOnHost;
    }

    public Path getCaCertOnHost() {
        return caCertOnHost;
    }
}
