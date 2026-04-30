package no.vegvesen.ixn.federation.adminserver.model.neighbour;

import java.util.Set;

public class SubscriptionApi implements Comparable<SubscriptionApi> {


    private Integer id;

    private SubscriptionStatusApi subscriptionStatus;

    private String selector;

    private String path;

    private int numberOfPolls;

    private String consumerCommonName;

    private Set<EndpointApi> endpoints;

    private Long lastUpdatedTimestamp;

    public SubscriptionApi() {
    }

    public SubscriptionApi(Integer id, SubscriptionStatusApi subscriptionStatus, String selector, String path, int numberOfPolls, String consumerCommonName, Set<EndpointApi> endpoints, Long lastUpdatedTimestamp) {
        this.id = id;
        this.subscriptionStatus = subscriptionStatus;
        this.selector = selector;
        this.path = path;
        this.numberOfPolls = numberOfPolls;
        this.consumerCommonName = consumerCommonName;
        this.endpoints = endpoints;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SubscriptionStatusApi getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(SubscriptionStatusApi subscriptionStatus) {
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

    public int getNumberOfPolls() {
        return numberOfPolls;
    }

    public void setNumberOfPolls(int numberOfPolls) {
        this.numberOfPolls = numberOfPolls;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public Set<EndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<EndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public int compareTo(SubscriptionApi o) {
        if (lastUpdatedTimestamp == null && o.getLastUpdatedTimestamp() == null) {
            return 0;
        }

        if (o.lastUpdatedTimestamp == null){
            return 1;
        }

        if (lastUpdatedTimestamp == null) {
            return -1;
        }

        return Long.compare(lastUpdatedTimestamp, o.lastUpdatedTimestamp);
    }
    @Override
    public String toString() {
        return "SubscriptionApi{" +
                "id=" + id +
                ", subscriptionStatus=" + subscriptionStatus +
                ", selector='" + selector + '\'' +
                ", path='" + path + '\'' +
                ", numberOfPolls=" + numberOfPolls +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", endpoints=" + endpoints +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                '}';
    }
}
