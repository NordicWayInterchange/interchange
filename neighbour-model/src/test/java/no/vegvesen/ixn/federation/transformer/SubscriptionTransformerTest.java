package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;
import no.vegvesen.ixn.federation.model.NeighbourSubscriptionStatus;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionTransformerTest {

    private final SubscriptionTransformer transformer = new SubscriptionTransformer();

    @Test
    public void testTransformSubscriptionStatusApiToSubscriptionStatus() {
        assertThat(transformer.subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi.REQUESTED)).isEqualTo(SubscriptionStatus.REQUESTED);
        assertThat(transformer.subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi.CREATED)).isEqualTo(SubscriptionStatus.CREATED);
        assertThat(transformer.subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi.ILLEGAL)).isEqualTo(SubscriptionStatus.ILLEGAL);

        //TODO is this correct? Test it! We never return NOT_VALID. Should we?
        assertThat(transformer.subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi.NOT_VALID)).isEqualTo(SubscriptionStatus.TEAR_DOWN);
        assertThat(transformer.subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi.NO_OVERLAP)).isEqualTo(SubscriptionStatus.NO_OVERLAP);
        assertThat(transformer.subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi.RESUBSCRIBE)).isEqualTo(SubscriptionStatus.RESUBSCRIBE);
        assertThat(transformer.subscriptionStatusApiToSubscriptionStatus(SubscriptionStatusApi.ERROR)).isEqualTo(SubscriptionStatus.TEAR_DOWN);
    }

    @Test
    public void testTransformNeighbourSubscriptionStatusToSubscriptionStatusApi() {
        assertThat(transformer.neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus.REQUESTED)).isEqualTo(SubscriptionStatusApi.REQUESTED);
        assertThat(transformer.neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus.ACCEPTED)).isEqualTo(SubscriptionStatusApi.REQUESTED);
        assertThat(transformer.neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus.CREATED)).isEqualTo(SubscriptionStatusApi.CREATED);
        assertThat(transformer.neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus.ILLEGAL)).isEqualTo(SubscriptionStatusApi.ILLEGAL);
        assertThat(transformer.neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus.NOT_VALID)).isEqualTo(SubscriptionStatusApi.NOT_VALID);
        assertThat(transformer.neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus.NO_OVERLAP)).isEqualTo(SubscriptionStatusApi.NO_OVERLAP);
        assertThat(transformer.neighbourSubscriptionStatusToSubscriptionStatusApi(NeighbourSubscriptionStatus.TEAR_DOWN)).isEqualTo(SubscriptionStatusApi.ERROR);

    }
}
