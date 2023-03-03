package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
public class SubscriptionTest {

    @Test
    public void testSettingEndpointsShouldRetainExistingOnes() {
        Endpoint firstEndpoint = new Endpoint(
                1,
                "source",
                "host",
                123
        );
        Endpoint secondEndpoint = new Endpoint(
                2,
                "source2",
                "host2",
                123
        );
        Subscription subscription =  new Subscription(
                SubscriptionStatus.CREATED,
                "a = b",
                "/blah",
                "my-ixn",
                new HashSet<>(Arrays.asList(
                        firstEndpoint,
                        secondEndpoint
                ))
        );
        subscription.setEndpoints(
                Collections.singleton(new Endpoint(
                        "source",
                        "host",
                        123
                ))
        );
        assertThat(subscription.getEndpoints()).hasSize(1);
        assertThat(subscription.getEndpoints()).allMatch(e -> e.getId().equals(1));
    }

    @Test
    public void addingNewEndpointToExistingSetReplacesIt() {
        Endpoint endpoint = new Endpoint(
                1,
                "source",
                "host",
                123
        );
        Subscription subscription =  new Subscription(
                SubscriptionStatus.CREATED,
                "a = b",
                "/blah",
                "my-ixn",
                Collections.singleton(
                        endpoint
                )
        );
        Endpoint secondEndpoint = new Endpoint(
                "source2",
                "host2",
                124
        );
        subscription.setEndpoints(
                Collections.singleton(secondEndpoint)
        );
        assertThat(subscription.getEndpoints()).hasSize(1);
        assertThat(subscription.getEndpoints()).containsExactly(secondEndpoint);
    }

    @Test
    public void addingSeveralEndpointsToSingeltonSet() {
        Endpoint firstEndpoint = new Endpoint(
                1,
                "source",
                "host",
                123
        );
        Subscription subscription =  new Subscription(
                SubscriptionStatus.CREATED,
                "a = b",
                "/blah",
                "my-ixn",
                new HashSet<>(Arrays.asList(
                        firstEndpoint
                ))
        );
        Endpoint secondEndpoint = new Endpoint(
                "source2",
                "host2",
                123
        );
        Endpoint thirdEndpoint = new Endpoint(
                "source3",
                "host3",
                134
        );
        subscription.setEndpoints(new HashSet<>(Arrays.asList(secondEndpoint,thirdEndpoint)));
        assertThat(subscription.getEndpoints()).hasSize(2);
        assertThat(subscription.getEndpoints()).doesNotContain(firstEndpoint);
    }

    @Test
    public void addingSeveralEndpointsToEmptySet() {
        Subscription subscription =  new Subscription(
                SubscriptionStatus.CREATED,
                "a = b",
                "/blah",
                "my-ixn",
                Collections.emptySet()
        );
        Endpoint firstEndpoint = new Endpoint(
                "source",
                "host",
                123
        );
        Endpoint secondEndpoint = new Endpoint(
                "source2",
                "host2",
                123
        );
        subscription.setEndpoints(new HashSet<>(Arrays.asList(firstEndpoint,secondEndpoint)));
        assertThat(subscription.getEndpoints()).hasSize(2);
        assertThat(subscription.getEndpoints()).containsExactly(firstEndpoint,secondEndpoint);
    }
}
