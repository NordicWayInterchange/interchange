package no.vegvesen.ixn.docker.keygen;

import no.vegvesen.ixn.docker.keygen.builder.ClusterBuilder;

import java.util.List;

public class Cluster {
    private final TopDomain topDomain;
    private final List<Interchange> interchanges;

    public Cluster(TopDomain topDomain, List<Interchange> interchanges) {
        this.topDomain = topDomain;
        this.interchanges = interchanges;
    }

    public static ClusterBuilder builder() {
        return new ClusterBuilder();
    }

    public TopDomain getTopDomain() {
        return topDomain;
    }

    public List<Interchange> getInterchanges() {
        return interchanges;
    }
}
