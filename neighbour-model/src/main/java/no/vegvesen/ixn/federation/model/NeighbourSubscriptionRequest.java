package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.NeighbourSubscriptionNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "neighbour_subscription_request")
public class NeighbourSubscriptionRequest {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRequest.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neigh_subreq_seq")
    @Column(name = "id")
    private Integer subreq_id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "subreq_id", foreignKey = @ForeignKey(name = "fk_neigh_sub_subreq"))
    private Set<NeighbourSubscription> subscription = new HashSet<>();

    private LocalDateTime successfulRequest;

    public NeighbourSubscriptionRequest() {
    }

    public NeighbourSubscriptionRequest(Set<NeighbourSubscription> subscription) {
        this.subscription.addAll(subscription);
    }

    public Set<NeighbourSubscription> getSubscriptions() {
        return subscription;
    }

    public void setSubscriptions(Set<NeighbourSubscription> newSubscription) {
        this.subscription.clear();
        if (newSubscription != null) {
            this.subscription.addAll(newSubscription);
        }
    }

    public void deleteSubscription (UUID subscriptionId) {
        NeighbourSubscription subscriptionToRemove = getSubscriptionByUuid(subscriptionId);
        subscription.remove(subscriptionToRemove);
    }

    public void deleteSubscriptions(Set<NeighbourSubscription> subscriptionsToDelete) {
        subscription.removeAll(subscriptionsToDelete);
    }

    public void setTearDownSubscription(UUID subscriptionId) {
        NeighbourSubscription subscriptionToTearDown = getSubscriptionByUuid(subscriptionId);
        subscriptionToTearDown.setSubscriptionStatus(NeighbourSubscriptionStatus.TEAR_DOWN);
    }
    public void addNewSubscriptions (Set<NeighbourSubscription> newSubscriptions) {
        subscription.addAll(newSubscriptions);
    }

    public NeighbourSubscription getSubscriptionByUuid(UUID id) throws NeighbourSubscriptionNotFound {

        for (NeighbourSubscription subscription : subscription) {
            if (subscription.getUuid().equals(id)) {
                return subscription;
            }
        }
        throw new NeighbourSubscriptionNotFound("Could not find subscription with id " + id);
    }
    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "subreq_id=" + subreq_id +
                ", subscription=" + subscription +
                '}';
    }

    public Set<NeighbourSubscription> getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus status) {
        return getSubscriptions().stream()
                .filter(s -> s.getSubscriptionStatus().equals(status))
                .collect(Collectors.toSet());
    }

    public boolean hasTearDownSubscriptions() {
        return !getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus.TEAR_DOWN).isEmpty();
    }

    public Optional<LocalDateTime> getSuccessfulRequest() {
        return Optional.ofNullable(successfulRequest);
    }

    public void setSuccessfulRequest(LocalDateTime successfulRequest) {
        this.successfulRequest = successfulRequest;
    }

    public boolean hasOtherConsumerCommonName(String ixnName) {
        return subscription.stream()
                .anyMatch(s -> !s.getConsumerCommonName().equals(ixnName));
    }

    public Set<NeighbourSubscription> getAcceptedSubscriptionsWithOtherConsumerCommonName(String ixnName) {
        return getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus.ACCEPTED).stream()
                .filter(s -> !s.getConsumerCommonName().equals(ixnName))
                .collect(Collectors.toSet());
    }
}
