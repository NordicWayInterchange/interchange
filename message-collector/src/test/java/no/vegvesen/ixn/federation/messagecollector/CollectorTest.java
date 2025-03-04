package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.Connection;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
        TimeUnit.MILLISECONDS.sleep(500);
        runnableA.stop();
        runnableB.stop();
        collector.shutdown();
        assertThat(runnableA.isDone()).isTrue();
        assertThat(runnableB.isDone()).isTrue();

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
                done.set(true);
                System.out.println("Interrupted");
                Thread.currentThread().interrupt();
            }
        }

        public long getCount() {
            return latch.getCount();
        }

        public boolean isDone() {
            return done.get();
        }

        public void stop() {
            done.set(true);
        }

    }
}
