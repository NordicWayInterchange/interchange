package no.vegvesen.ixn.docker.keygen.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CARequest {

    private final String name;
    private final String country;
    private final List<CARequest> subCaRequests = new ArrayList<>();
    private final List<HostRequest> hostRequests = new ArrayList<>();
    private final List<ClientRequest> clientRequests = new ArrayList<>();

    public CARequest(String name, String country, List<CARequest> subCaRequests, List<HostRequest> hostRequests, List<ClientRequest> clientRequests) {
        this.name = name;
        this.country = country;
        this.subCaRequests.addAll(subCaRequests);
        this.hostRequests.addAll(hostRequests);
        this.clientRequests.addAll(clientRequests);
    }


    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public List<CARequest> getSubCas() {
        return Collections.unmodifiableList(subCaRequests);
    }

    public List<HostRequest> getHosts() {
        return Collections.unmodifiableList(hostRequests);
    }

    public List<ClientRequest> getClients() {
        return Collections.unmodifiableList(clientRequests);
    }
}
