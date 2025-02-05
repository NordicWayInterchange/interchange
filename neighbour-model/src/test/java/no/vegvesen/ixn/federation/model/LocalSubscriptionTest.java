package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
public class LocalSubscriptionTest {

    @Test
    public void tearDownSubscriptionIsNotAlive() {
        LocalSubscription sub = new LocalSubscription("publicationId = 'pub-1'", "neighbour", "Subscription");
        sub.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
        assertThat(LocalSubscriptionStatus.isAlive(sub.getStatus())).isFalse();
    }

    @Test
    public void illegalSubscriptionIsNotAlive() {
        LocalSubscription sub = new LocalSubscription("publicationId = 'pub-1'", "neighbour", "Subscription");
        sub.setStatus(LocalSubscriptionStatus.ILLEGAL);
        assertThat(LocalSubscriptionStatus.isAlive(sub.getStatus())).isFalse();
    }

    @Test
    public void createdSubscriptionIsAlive() {
        LocalSubscription sub = new LocalSubscription("publicationId = 'pub-1'", "neighbour", "Subscription");
        sub.setStatus(LocalSubscriptionStatus.CREATED);
        assertThat(LocalSubscriptionStatus.isAlive(sub.getStatus())).isTrue();
    }
}
