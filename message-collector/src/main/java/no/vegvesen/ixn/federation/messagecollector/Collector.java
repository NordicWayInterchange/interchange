package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.ListenerEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Collector {

    private final ExecutorService executorService;
    private final List<ListenerEndpoint> states;

    public Collector() {
        executorService = Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory());
        states = new ArrayList<>();
    }


    public void submitEndpoint(final ListenerEndpoint endpoint, Runnable task) {
        states.add(endpoint);
        executorService.execute(task);
    }

    public boolean containsEndpoint(final ListenerEndpoint endpoint) {
        return states.contains(endpoint);
    }


    public void shutdown() {
        executorService.shutdown();
    }

    //public record ListenerState(ListenerEndpoint endpoint, Future<?> future) {}
}
