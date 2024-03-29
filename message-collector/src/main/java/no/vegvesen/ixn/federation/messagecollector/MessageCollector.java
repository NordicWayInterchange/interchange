package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class MessageCollector {

    private final CollectorCreator collectorCreator;
    private GracefulBackoffProperties backoffProperties;
    private ListenerEndpointRepository listenerEndpointRepository;
    //NOTE: This is implicitly thread safe. If more than one thread can access the listeners map, the implementation of the listener Map will have to change.
    private Map<ListenerEndpoint, MessageCollectorListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageCollector.class);


    @Autowired
    public MessageCollector(ListenerEndpointRepository listenerEndpointRepository, CollectorCreator collectorCreator, GracefulBackoffProperties backoffProperties) {
        this(listenerEndpointRepository,collectorCreator,backoffProperties,new HashMap<>());
    }

    public MessageCollector(ListenerEndpointRepository listenerEndpointRepository, CollectorCreator collectorCreator, GracefulBackoffProperties backoffProperties, Map<ListenerEndpoint,MessageCollectorListener> listeners) {
        this.listenerEndpointRepository = listenerEndpointRepository;
        this.collectorCreator = collectorCreator;
        this.backoffProperties = backoffProperties;
        this.listeners = listeners;
    }

    @Scheduled(fixedRateString = "${collector.fixeddelay}")
    public void runSchedule() {
        checkListenerList();
        setupConnectionsToNewNeighbours();
    }

    public void checkListenerList() {
        //TODO logging of the entries...
        List<ListenerEndpoint> entriesToRemove = listeners.entrySet().stream().filter(e -> !e.getValue().isRunning()).map(e -> e.getKey()).collect(Collectors.toList());
        listeners.keySet().removeAll(entriesToRemove);
        logger.info("Removed listeners {}", entriesToRemove);
    }

    public void setupConnectionsToNewNeighbours() {
        List<ListenerEndpoint> listenerEndpoints = listenerEndpointRepository.findAll();
        Set<ListenerEndpoint> interchangeListenerEndpoints = new HashSet<>();

        for (ListenerEndpoint listenerEndpoint : listenerEndpoints) {
            String neighbourName = listenerEndpoint.getNeighbourName();
            String hostName = listenerEndpoint.getHost();
            interchangeListenerEndpoints.add(listenerEndpoint);
            if (!listeners.containsKey(listenerEndpoint)) {
                setUpConnectionToNeighbour(listenerEndpoint);
            }
            else {
                if (listeners.get(listenerEndpoint).isRunning()) {
                    logger.debug("Listener for {} with host {} is still running with no changes", neighbourName, hostName);
                } else {
                    logger.debug("Non-running listener detected, name {} with host {}", neighbourName, hostName);
                }
            }
        }

        List<ListenerEndpoint> listenerKeysToRemove = new ArrayList<>();

        for (ListenerEndpoint listenerEndpoint : listeners.keySet()) {
            if (!interchangeListenerEndpoints.contains(listenerEndpoint)) {
                String neighbourName = listenerEndpoint.getNeighbourName();
                logger.info("Listener for {} with host {} and port {} is now being removed", neighbourName, listenerEndpoint.getHost(), listenerEndpoint.getPort());
                MessageCollectorListener toRemove = listeners.get(listenerEndpoint);
                logger.debug("Tearing down listener for {} with host {} and port {}", neighbourName, listenerEndpoint.getHost(), listenerEndpoint.getPort());
                toRemove.teardown();
                listenerKeysToRemove.add(listenerEndpoint);
            }
        }

        listeners.keySet().removeAll(listenerKeysToRemove);
    }

    public void setUpConnectionToNeighbour(ListenerEndpoint listenerEndpoint){
        String name = listenerEndpoint.getNeighbourName();
        if(listenerEndpoint.getMessageConnection().canBeContacted(backoffProperties)) {
            try {
                logger.info("Setting up connection to ixn with name {}, host {} and port {}", name, listenerEndpoint.getHost(), listenerEndpoint.getPort());
                MessageCollectorListener messageListener = collectorCreator.setupCollection(listenerEndpoint);
                listeners.put(listenerEndpoint, messageListener);
                listenerEndpoint.getMessageConnection().okConnection();
            } catch (MessageCollectorException e) {
                logger.warn("Tried to create connection to {} with host {} and port {}, but failed with exception.", name, listenerEndpoint.getHost(), listenerEndpoint.getPort(), e);
                listenerEndpoint.getMessageConnection().failedConnection(backoffProperties.getNumberOfAttempts());
            }
        }
        else {
            logger.info("Too soon to connect to {} with host {} and port {}", name, listenerEndpoint.getHost(), listenerEndpoint.getPort());
        }
    }

    Map<ListenerEndpoint, MessageCollectorListener> getListeners() {
        return listeners;
    }
}
