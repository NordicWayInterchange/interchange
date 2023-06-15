package no.vegvesen.ixn.docker.keygen.generator;

import java.nio.file.Path;

public class CsrKeyPair {
    private final Path csrOnHost;
    private final Path keyOnHost;

    public CsrKeyPair(Path csrOnHost, Path keyOnHost) {

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
