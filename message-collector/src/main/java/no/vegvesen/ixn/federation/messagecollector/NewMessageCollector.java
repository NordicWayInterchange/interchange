package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.ConnectionCreator;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;

import javax.net.ssl.SSLContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NewMessageCollector {
    private final Map<ListenerEndpoint, MessageForwarder> states;
    private final SSLContext senderContext;
    private final ConnectionCreator connectionCreator;
    private final ExecutorService executorService;

    public NewMessageCollector(SSLContext senderContext, ConnectionCreator connectionCreator) {
        this.senderContext = senderContext;
        this.states = new HashMap<>();
        this.connectionCreator = connectionCreator;
        this.executorService = Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory());
    }

    public void syncListeners(List<ListenerEndpoint> endpoints, String localUrl) {
        addToExecution(endpoints, localUrl);
        removeSpareListeners(endpoints);
    }

    public int numberOfListeners() {
        return states.size();
    }

    public void removeSpareListeners(List<ListenerEndpoint> desiredEndpoints) {
        for (ListenerEndpoint listenerEndpoint : states.keySet()) {
            if (!desiredEndpoints.contains(listenerEndpoint)) {
                MessageForwarder messageForwarder = states.get(listenerEndpoint);
                messageForwarder.stop();
                states.remove(listenerEndpoint);
            }
        }
    }

    public void addToExecution(List<ListenerEndpoint> endpoints, String localUrl) {
        for (ListenerEndpoint endpoint : endpoints) {
            if (!states.containsKey(endpoint)) {
                MessageForwarder forwarder = new MessageForwarder(
                        localUrl,
                        endpoint.getTarget(),
                        senderContext,
                        connectionCreator,
                        endpoint.toUrl(),
                        endpoint.getSource()
                );
                executorService.execute(forwarder);
                states.put(endpoint, forwarder);
            }
        }
    }


}
