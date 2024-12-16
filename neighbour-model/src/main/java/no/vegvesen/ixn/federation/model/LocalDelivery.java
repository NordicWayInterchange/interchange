package no.vegvesen.ixn.federation.model;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "local_deliveries")
public class LocalDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locdel_seq")
    @Column(name="id")
    private Integer id;

    @Column
    private String uuid = UUID.randomUUID().toString();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "locdelend_id", foreignKey = @ForeignKey(name = "fk_locdel_end"))
    private Set<LocalDeliveryEndpoint> endpoints = new HashSet<>();

    @JoinColumn(name = "sel_id", foreignKey = @ForeignKey(name = "fk_locdel_sel"))
    @Column(columnDefinition="TEXT")
    private String selector = "";

    @Column
    @UpdateTimestamp
    private LocalDateTime lastUpdatedTimestamp;

    private String exchangeName = "";

    @Enumerated(EnumType.STRING)
    private LocalDeliveryStatus status = LocalDeliveryStatus.REQUESTED;

    @Column
    private String description;

    @Column
    private String errorMessage;

    public LocalDelivery() {
    }

    public LocalDelivery(Integer id, Set<LocalDeliveryEndpoint> endpoints,  String selector, LocalDeliveryStatus status) {
        this.id = id;
        this.endpoints.addAll(endpoints);
        this.selector = selector;
        this.status = status;
    }

    public LocalDelivery(Integer id, Set<LocalDeliveryEndpoint> endpoints,  String selector, LocalDeliveryStatus status, String description) {
        this.id = id;
        this.endpoints.addAll(endpoints);
        this.selector = selector;
        this.status = status;
        this.description = description;
    }

    public LocalDelivery(String uuid, Set<LocalDeliveryEndpoint> endpoints, String selector, LocalDeliveryStatus status) {
        this.uuid = uuid;
        this.endpoints.addAll(endpoints);
        this.selector = selector;
        this.status = status;
    }

    public LocalDelivery(Integer id, String selector, LocalDeliveryStatus status) {
        this(id, Collections.emptySet(),selector,status);
    }

    public LocalDelivery(String selector, String description){
        this.selector = selector;
        this.description = description;
    }

    public LocalDelivery(String selector, LocalDeliveryStatus status, String description) {
        this.selector = selector;
        this.status = status;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<LocalDeliveryEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Set<LocalDeliveryEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void addEndpoints(Set<LocalDeliveryEndpoint> endpoints) {
        this.endpoints.addAll(endpoints);
    }

    public void addEndpoint(LocalDeliveryEndpoint endpoint) {
        this.endpoints.add(endpoint);
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public LocalDateTime getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(LocalDateTime lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public LocalDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(LocalDeliveryStatus status) {
        this.status = status;
    }

    public void removeAllEndpoints(Set<LocalDeliveryEndpoint> endpointsToRemove) {
        this.endpoints.removeAll(endpointsToRemove);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void removeEndpoint(LocalDeliveryEndpoint endpoint) {
        this.endpoints.remove(endpoint);
    }

    public boolean exchangeExists() {
        return !exchangeName.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalDelivery delivery = (LocalDelivery) o;
        return  Objects.equals(selector, delivery.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector);
    }

    @Override
    public String toString() {
        return "LocalDelivery{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", endpoints=" + endpoints +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", exchangeName='" + exchangeName + '\'' +
                ", status=" + status +
                ", errorMessage=" + errorMessage +
                ", description=" + description +
                '}';
    }
}
