package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "matches_seq")
    private Integer id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "loc_sub", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_match_local_subscription"))
    private LocalSubscription localSubscription;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "sub", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_match_subscription"))
    private Subscription subscription;

    private String serviceProviderName;

    public Match() {

    }

    public Match(LocalSubscription localSubscription, Subscription subscription) {
        this.localSubscription = localSubscription;
        this.subscription = subscription;
    }

    public Match(LocalSubscription localSubscription, Subscription subscription, String serviceProviderName) {
        this.localSubscription = localSubscription;
        this.subscription = subscription;
        this.serviceProviderName = serviceProviderName;
    }

    public LocalSubscription getLocalSubscription() {
        return localSubscription;
    }

    public void setLocalSubscription(LocalSubscription localSubscription) {
        this.localSubscription = localSubscription;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public boolean subscriptionIsTearDown() {
        return subscription.getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN);
    }

    public boolean localSubscriptionIsTearDown() {
        return !localSubscription.isSubscriptionWanted();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match)) return false;
        Match match = (Match) o;
        return localSubscription.equals(match.localSubscription) &&
                subscription.equals(match.subscription) &&
                serviceProviderName.equals(match.serviceProviderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localSubscription, subscription, serviceProviderName);
    }

    @Override
    public String toString() {
        return "Match{" +
                "localSubscription=" + localSubscription +
                ", subscription=" + subscription +
                '}';
    }

    public Integer getId() {
        return id;
    }
}
