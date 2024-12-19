package no.vegvesen.ixn.keys.stores;

import java.nio.file.Path;

//A CaResponse gives a truststore,
public record CaStore(String name, Path path, String password) {
}
