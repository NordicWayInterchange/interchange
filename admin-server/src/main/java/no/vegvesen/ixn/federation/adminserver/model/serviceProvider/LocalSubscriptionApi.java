package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import no.vegvesen.ixn.federation.model.LocalConnection;

import java.util.HashSet;
import java.util.Set;

public class LocalSubscriptionApi {

    private String id;

    private LocalSubscriptionStatusApi status;

    private String selector;

    private Set<LocalSubscriptionEndpointApi> endpoints;

    private Long lastUpdated;

    private String consumerCommonName;

    private Set<LocalConnection> connections = new HashSet<>();

    private String description;

    private String errorMessage;


    public LocalSubscriptionApi(String id, LocalSubscriptionStatusApi status, String selector, String consumerCommonName,
                                String description, String errorMessage, Set<LocalConnection> connections, Set<LocalSubscriptionEndpointApi> endpoints, Long lastUpdated) {
        this.id = id;
        this.status = status;
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.description = description;
        this.connections.addAll(connections);
        this.errorMessage = errorMessage;
        this.endpoints = endpoints;
        this.lastUpdated = lastUpdated;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public LocalSubscriptionStatusApi getStatus() {
        return status;
    }

    public void setStatus(LocalSubscriptionStatusApi status) {
        this.status = status;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }


    public Set<LocalSubscriptionEndpointApi> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<LocalSubscriptionEndpointApi> endpoints) {
        this.endpoints = endpoints;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Set<LocalConnection> getConnections() {
        return connections;
    }

    public void setConnections(Set<LocalConnection> connections) {
        this.connections = connections;
    }

    public void addConnection(LocalConnection connection) {
        connections.add(connection);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ServiceProviderSubscriptionApi{" +
                "id=" + id +
                ", status=" + status +
                ", selector='" + selector + '\'' +
                ", endpoints=" + endpoints +
                ", consumerCommonName=" + consumerCommonName +
                ", errorMessage=" + errorMessage +
                ", description=" + description +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
