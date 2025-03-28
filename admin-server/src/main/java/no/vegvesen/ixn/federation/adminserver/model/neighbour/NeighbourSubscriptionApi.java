package no.vegvesen.ixn.federation.adminserver.model.neighbour;

import java.util.Set;

public class NeighbourSubscriptionApi implements Comparable<NeighbourSubscriptionApi>{

    private Integer subreq_id;

    private NeighbourSubscriptionStatusApi subscriptionStatus;

    private String selector;

    private String path;

    private String consumerCommonName;

    private Set<NeighbourEndpointApi> endpoints;

    private Long lastUpdatedTimestamp;

    public NeighbourSubscriptionApi() {
    }

    public NeighbourSubscriptionApi(Integer subreq_id, NeighbourSubscriptionStatusApi subscriptionStatus, String selector, String path, String consumerCommonName, Set<NeighbourEndpointApi> endpoints, Long lastUpdatedTimestamp) {
        this.subreq_id = subreq_id;
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.path = path;
        this.consumerCommonName = consumerCommonName;
        this.endpoints = endpoints;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }


    public Integer getSubreq_id() {
        return subreq_id;
    }

    public void setSubreq_id(Integer subreq_id) {
        this.subreq_id = subreq_id;
    }


    public NeighbourSubscriptionStatusApi getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(NeighbourSubscriptionStatusApi subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public Set<NeighbourEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<NeighbourEndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public int compareTo(NeighbourSubscriptionApi o) {
        if(lastUpdatedTimestamp == null && o.lastUpdatedTimestamp == null){
            return 0;
        }
        if(o.lastUpdatedTimestamp == null){
            return 1;
        }
        if(lastUpdatedTimestamp == null){
            return -1;
        }
        return Long.compare(lastUpdatedTimestamp, o.lastUpdatedTimestamp);
    }

    @Override
    public String toString() {
        return "NeighbourSubscriptionApi{" +
                "id=" + subreq_id +
                ", subscriptionStatus=" + subscriptionStatus +
                ", selector='" + selector + '\'' +
                ", path='" + path + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }


}
