package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.Connection;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectorTest {

    @Test
    public void foo() throws InterruptedException {
        ListenerEndpoint endpointA = new ListenerEndpoint(
                "neighbourA",
                "sourceA",
                "hostA",
                1,
                new Connection(),
                "targetA"
        );
        ListenerEndpoint endpointB = new ListenerEndpoint(
                "neighbourB",
                "sourceB",
                "hostB",
                1,
                new Connection(),
                "targetB"
        );
        Collector collector = new Collector();
        SleepingRunnable runnableA = new SleepingRunnable();
        SleepingRunnable runnableB = new SleepingRunnable();
        collector.submitEndpoint(endpointA, runnableA);
        collector.submitEndpoint(endpointB, runnableB);
        //assertThat(runnableA.getCount()).isEqualTo(1);
        assertThat(runnableA.isDone()).isTrue();
        collector.shutdown();
        assertThat(runnableB.isDone()).isTrue();
        //assertThat(runnableB.getCount()).isEqualTo(0);



    }

    public static class SleepingRunnable implements Runnable {
        private CountDownLatch latch = new CountDownLatch(1);
        private AtomicBoolean done = new AtomicBoolean(false);

        @Override
        public void run() {
            try {
                System.out.println("Running");
                latch.await();
            } catch (InterruptedException e) {
                latch.countDown();
                System.out.println("Interrupted");
            } finally {
                done.set(true);
            }
        }

        public long getCount() {
            return latch.getCount();
        }

        public boolean isDone() {
            return done.get();
        }

    }
}
