package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "matches_seq")
    private Integer id;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "loc_sub", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_match_local_subscription"))
    private LocalSubscription localSubscription;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "sub", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_match_subscription"))
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    private String serviceProviderName;

    public Match() {

    }

    public Match(LocalSubscription localSubscription, Subscription subscription, MatchStatus status) {
        this.localSubscription = localSubscription;
        this.subscription = subscription;
        this.status = status;
    }

    public Match(LocalSubscription localSubscription, Subscription subscription, String serviceProviderName, MatchStatus status) {
        this.localSubscription = localSubscription;
        this.subscription = subscription;
        this.serviceProviderName = serviceProviderName;
        this.status = status;
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

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match)) return false;
        Match match = (Match) o;
        return localSubscription.equals(match.localSubscription) &&
                subscription.equals(match.subscription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localSubscription, subscription);
    }

    @Override
    public String toString() {
        return "Match{" +
                "localSubscription=" + localSubscription +
                ", subscription=" + subscription +
                '}';
    }
}
