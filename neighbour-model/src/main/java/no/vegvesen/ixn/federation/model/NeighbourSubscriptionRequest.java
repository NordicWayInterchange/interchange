package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.NeighbourSubscriptionNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "neighbour_subscription_request")
public class NeighbourSubscriptionRequest {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRequest.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neigh_subreq_seq")
    @Column(name = "id")
    private Integer subreq_id;

    @Enumerated(EnumType.STRING)
    private NeighbourSubscriptionRequestStatus status = NeighbourSubscriptionRequestStatus.EMPTY;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "subreq_id", foreignKey = @ForeignKey(name = "fk_neigh_sub_subreq"))
    private Set<NeighbourSubscription> subscription = new HashSet<>();

    private LocalDateTime successfulRequest;

    public NeighbourSubscriptionRequest() {
    }

    public NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus status, Set<NeighbourSubscription> subscription) {
        this.status = status;
        this.subscription = subscription;
    }

    public NeighbourSubscriptionRequestStatus getStatus() {
        return status;
    }

    public void setStatus(NeighbourSubscriptionRequestStatus status) {
        this.status = status;
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

    public void deleteSubscription (Integer subscriptionId) {
        NeighbourSubscription subscriptionToRemove = getSubscriptionById(subscriptionId);
        subscription.remove(subscriptionToRemove);
    }

    public void deleteSubscriptions(Set<NeighbourSubscription> subscriptionsToDelete) {
        subscription.removeAll(subscriptionsToDelete);
    }

    public void setTearDownSubscription(Integer subscriptionId) {
        NeighbourSubscription subscriptionToTearDown = getSubscriptionById(subscriptionId);
        subscriptionToTearDown.setSubscriptionStatus(NeighbourSubscriptionStatus.TEAR_DOWN);
    }

    public void addNewSubscriptions (Set<NeighbourSubscription> newSubscriptions) {
        subscription.addAll(newSubscriptions);
    }

    public NeighbourSubscription getSubscriptionById(Integer id) throws NeighbourSubscriptionNotFound {

        for (NeighbourSubscription subscription : subscription) {
            if (subscription.getId().equals(id)) {
                return subscription;
            }
        }

        throw new NeighbourSubscriptionNotFound("Could not find subscription with id " + id);
    }

    @Override
    public String toString() {
        return "SubscriptionRequest{" +
                "subreq_id=" + subreq_id +
                ", status=" + status +
                ", subscription=" + subscription +
                '}';
    }

    public Set<NeighbourSubscription> getAcceptedSubscriptions() {
        return getSubscriptions().stream()
                .filter(s -> s.getSubscriptionStatus().equals(NeighbourSubscriptionStatus.ACCEPTED))
                .collect(Collectors.toSet());
    }

    public Set<NeighbourSubscription> getCreatedSubscriptions() {
        return getSubscriptions().stream()
                .filter(s -> s.getSubscriptionStatus().equals(NeighbourSubscriptionStatus.CREATED))
                .collect(Collectors.toSet());
    }

    public Set<NeighbourSubscription> getResubscribeSubscriptions() {
        return getSubscriptions().stream()
                .filter(s -> s.getSubscriptionStatus().equals(NeighbourSubscriptionStatus.RESUBSCRIBE))
                .collect(Collectors.toSet());
    }

    public Set<NeighbourSubscription> getTearDownSubscriptions() {
        return getSubscriptions().stream()
                .filter(s -> s.getSubscriptionStatus().equals(NeighbourSubscriptionStatus.TEAR_DOWN))
                .collect(Collectors.toSet());
    }

    public boolean hasTearDownSubscriptions() {
        return !getTearDownSubscriptions().isEmpty();
    }

    public Optional<LocalDateTime> getSuccessfulRequest() {
        return Optional.ofNullable(successfulRequest);
    }

    public void setSuccessfulRequest(LocalDateTime successfulRequest) {
        this.successfulRequest = successfulRequest;
    }

    public Set<String> wantedBindings() {
        Set<String> wantedBindings = new HashSet<>();
        for (NeighbourSubscription subscription : getAcceptedSubscriptions()) {
            wantedBindings.add(subscription.bindKey());
        }
        for (NeighbourSubscription subscription : getCreatedSubscriptions()) {
            wantedBindings.add(subscription.bindKey());
        }
        return wantedBindings;
    }

    public Set<String> getUnwantedBindKeys(Set<String> existingBindKeys) {
        Set<String> wantedBindKeys = this.wantedBindings();
        Set<String> unwantedBindKeys = new HashSet<>(existingBindKeys);
        unwantedBindKeys.removeAll(wantedBindKeys);
        return unwantedBindKeys;
    }

    public boolean hasOtherConsumerCommonName(String ixnName) {
        return subscription.stream()
                .anyMatch(s -> !s.getConsumerCommonName().equals(ixnName));
    }

    public Set<NeighbourSubscription> getAcceptedSubscriptionsWithOtherConsumerCommonName(String ixnName) {
        return getAcceptedSubscriptions().stream()
                .filter(s -> !s.getConsumerCommonName().equals(ixnName))
                .collect(Collectors.toSet());
    }

    public Set<NeighbourSubscription> getAcceptedSubscriptionsWithSameConsumerCommonNameAsIxn(String ixnName) {
        return getAcceptedSubscriptions().stream()
                .filter(s -> s.getConsumerCommonName().equals(ixnName))
                .collect(Collectors.toSet());
    }
}
