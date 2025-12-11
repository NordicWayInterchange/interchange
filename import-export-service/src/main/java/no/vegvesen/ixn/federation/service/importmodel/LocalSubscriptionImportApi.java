package no.vegvesen.ixn.federation.service.importmodel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalSubscriptionImportApi {

    private String uuid;

    private String selector;

    private String consumerCommonName;

    private LocalSubscriptionStatusImportApi status;

    private Set<LocalEndpointImportApi> localEndpoints;

    private Set<LocalConnectionImportApi> localConnections;

    private String description;


    public enum LocalSubscriptionStatusImportApi {
        REQUESTED, CREATED, TEAR_DOWN, ILLEGAL, NOT_VALID, RESUBSCRIBE, ERROR;

    }
    public LocalSubscriptionImportApi() {
    }
    public LocalSubscriptionImportApi(String uuid,
                                      String consumerCommonName,
                                      String selector,
                                      LocalSubscriptionStatusImportApi status,
                                      Set<LocalEndpointImportApi> localEndpoints,
                                      Set<LocalConnectionImportApi> localConnections,
                                      String description) {
        this.selector = selector;
        this.consumerCommonName = consumerCommonName;
        this.status = status;
        this.localEndpoints = localEndpoints;
        this.localConnections = localConnections;
        this.uuid = uuid;
        this.description = description;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSelector() {
        return selector;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getConsumerCommonName() {
        return consumerCommonName;
    }

    public void setConsumerCommonName(String consumerCommonName) {
        this.consumerCommonName = consumerCommonName;
    }

    public LocalSubscriptionStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(LocalSubscriptionStatusImportApi status) {
        this.status = status;
    }

    public Set<LocalEndpointImportApi> getLocalEndpoints() {
        return localEndpoints;
    }

    public void setLocalEndpoints(Set<LocalEndpointImportApi> localEndpoints) {
        this.localEndpoints = localEndpoints;
    }

    public Set<LocalConnectionImportApi> getLocalConnections() {
        return localConnections;
    }

    public void setLocalConnections(Set<LocalConnectionImportApi> localConnections) {
        this.localConnections = localConnections;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LocalSubscriptionImportApi that = (LocalSubscriptionImportApi) o;
        return Objects.equals(uuid, that.uuid) && Objects.equals(selector, that.selector) && Objects.equals(consumerCommonName, that.consumerCommonName) && status == that.status && Objects.equals(localEndpoints, that.localEndpoints) && Objects.equals(localConnections, that.localConnections) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, selector, consumerCommonName, status, localEndpoints, localConnections, description);
    }

    @Override
    public String toString() {
        return "LocalSubscriptionImportApi{" +
                "uuid='" + uuid + '\'' +
                ", selector='" + selector + '\'' +
                ", consumerCommonName='" + consumerCommonName + '\'' +
                ", status=" + status +
                ", localEndpoints=" + localEndpoints +
                ", localConnections=" + localConnections +
                ", description='" + description + '\'' +
                '}';
    }
}
