package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.Interchange;
import no.vegvesen.ixn.docker.keygen.TopDomain;

import java.util.ArrayList;
import java.util.List;

public class ClusterBuilder {
    private TopDomain topDomain;


    public TopDomainBuilder topDomain() {
        return new TopDomainBuilder(this);
    }

    public void addTopDomain(TopDomain topDomain) {
        this.topDomain = topDomain;
    }


    public Cluster done() {
        return new Cluster(topDomain);
    }
}
