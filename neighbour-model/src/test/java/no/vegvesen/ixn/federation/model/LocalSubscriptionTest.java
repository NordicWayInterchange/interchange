package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
public class LocalSubscriptionTest {

    @Test
    public void addingSeveralEquivalentLocalConnections() {
        LocalConnection connection1 = new LocalConnection(
                1,
                "source",
                "destination"
        );
        LocalSubscription localSubscription = new LocalSubscription(
                1,
                LocalSubscriptionStatus.REQUESTED,
                "a = b",
                "my-node",
                Collections.singleton(connection1),
                Collections.emptySet()
        );
        LocalConnection connection2 = new LocalConnection(
                "source",
                "destination"
        );
        localSubscription.addConnection(connection2);
        assertThat(localSubscription.getConnections()).hasSize(1);
        assertThat(localSubscription.getConnections()).allMatch( c -> Integer.valueOf(1).equals(c.getId()));

    }

    @Test
    public void tearDownSubscriptionIsNotAlive() {
        LocalSubscription sub = new LocalSubscription("publicationId = 'pub-1'", "neighbour");
        sub.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
        assertThat(LocalSubscriptionStatus.isAlive(sub.getStatus())).isFalse();
    }

    @Test
    public void illegalSubscriptionIsNotAlive() {
        LocalSubscription sub = new LocalSubscription("publicationId = 'pub-1'", "neighbour");
        sub.setStatus(LocalSubscriptionStatus.ILLEGAL);
        assertThat(LocalSubscriptionStatus.isAlive(sub.getStatus())).isFalse();
    }

    @Test
    public void createdSubscriptionIsAlive() {
        LocalSubscription sub = new LocalSubscription("publicationId = 'pub-1'", "neighbour");
        sub.setStatus(LocalSubscriptionStatus.CREATED);
        assertThat(LocalSubscriptionStatus.isAlive(sub.getStatus())).isTrue();
    }
}
