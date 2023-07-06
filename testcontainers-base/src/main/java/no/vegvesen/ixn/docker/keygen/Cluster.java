package no.vegvesen.ixn.docker.keygen;

import no.vegvesen.ixn.docker.keygen.builder.ClusterBuilder;

import java.util.List;

public class Cluster {
    private TopDomain topDomain;

    public Cluster() {

    }

    public Cluster(TopDomain topDomain) {
        this.topDomain = topDomain;
    }

    public static ClusterBuilder builder() {
        return new ClusterBuilder();
    }

    public TopDomain getTopDomain() {
        return topDomain;
    }

}
