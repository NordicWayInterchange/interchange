package no.vegvesen.ixn.docker.keygen;

public class Cluster {
    private TopDomain topDomain;

    public Cluster() {

    }

    public Cluster(TopDomain topDomain) {
        this.topDomain = topDomain;
    }

    public TopDomain getTopDomain() {
        return topDomain;
    }

}
