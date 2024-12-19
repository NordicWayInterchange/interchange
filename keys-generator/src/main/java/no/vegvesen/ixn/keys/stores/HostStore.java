package no.vegvesen.ixn.keys.stores;

import java.nio.file.Path;

//a HostResponse or a ClientResponse gives a keystore
public record HostStore(String hostname, Path path, String password) {
}
