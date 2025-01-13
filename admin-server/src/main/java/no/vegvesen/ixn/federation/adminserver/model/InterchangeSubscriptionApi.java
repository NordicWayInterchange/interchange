package no.vegvesen.ixn.federation.adminserver.model;

import java.util.Set;

public class InterchangeSubscriptionApi {

    private Integer subreq_id;

    private InterchangeSubscriptionStatusApi subscriptionStatus;

    private String selector;

    private String path;

    private String consumerCommonName;

    private Set<InterchangeEndpointApi> endpoints;

    private Long lastUpdatedTimestamp;

    public InterchangeSubscriptionApi() {
    }

    public InterchangeSubscriptionApi(Integer subreq_id, InterchangeSubscriptionStatusApi subscriptionStatus, String selector, String path, String consumerCommonName, Set<InterchangeEndpointApi> endpoints, Long lastUpdatedTimestamp) {
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


    public InterchangeSubscriptionStatusApi getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(InterchangeSubscriptionStatusApi subscriptionStatus) {
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

    public Set<InterchangeEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<InterchangeEndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    @Override
    public String toString() {
        return "InterchangeSubscriptionApi{" +
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
