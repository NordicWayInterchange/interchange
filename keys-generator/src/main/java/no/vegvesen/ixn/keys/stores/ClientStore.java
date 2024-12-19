package no.vegvesen.ixn.keys.stores;

import java.nio.file.Path;

public record ClientStore(String clientName, Path path, String password) {
}
