package no.vegvesen.ixn.docker.keygen.builder;

import no.vegvesen.ixn.docker.keygen.Cluster;
import no.vegvesen.ixn.docker.keygen.Interchange;
import no.vegvesen.ixn.docker.keygen.TopDomain;

import java.util.ArrayList;
import java.util.List;

public class ClusterBuilder {
    private TopDomain topDomain;
    private List<Interchange> interchanges = new ArrayList<>();


    public TopDomainBuilder topDomain() {
        return new TopDomainBuilder(this);
    }

    public void addTopDomain(TopDomain topDomain) {
        this.topDomain = topDomain;
    }

    public InterchangeBuilder interchange() {
        return new InterchangeBuilder(this);
    }

    public void addInterchange(Interchange interchange) {
        interchanges.add(interchange);

    }

    public Cluster done() {
        return new Cluster(topDomain, interchanges);
    }
}
