package no.vegvesen.ixn.keys.stores;

import java.util.List;

public record CaStores(String name, CaStore trustStore, List<HostStore> hostStores, List<ClientStore> clientStores,
                       List<CaStores> subCaStores) {
    public HostStore getHostStore(String hostname) {
        return hostStores.stream().filter(h -> h.hostname().equals(hostname)).findAny().orElseThrow(() -> new RuntimeException("No store found for hostname: " + hostname));
    }

    public ClientStore getClientStore(String clientName) {
        return clientStores.stream().filter(c -> c.clientName().equals(clientName)).findAny().orElseThrow(() -> new RuntimeException("No client store found for " + clientName));
    }

    public String getClientStorePath(String clientName) {
        return getClientStore(clientName).path().toString();
    }

    public String getTrustStorePath() {
        return trustStore.path().toString();
    }
}
