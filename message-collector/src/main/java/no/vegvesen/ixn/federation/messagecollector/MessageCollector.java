package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class MessageCollector {

    private NeighbourService neighbourService;
    private final CollectorCreator collectorCreator;
    private GracefulBackoffProperties backoffProperties;
    private ListenerEndpointRepository listenerEndpointRepository;

    //NOTE: This is implicitly thread safe. If more than one thread can access the listeners map, the implementation of the listener Map will have to change.
    private Map<String, MessageCollectorListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageCollector.class);


    @Autowired
    public MessageCollector(ListenerEndpointRepository listenerEndpointRepository, CollectorCreator collectorCreator, GracefulBackoffProperties backoffProperties) {
        this.listenerEndpointRepository = listenerEndpointRepository;
        this.collectorCreator = collectorCreator;
        this.listeners = new HashMap<>();
        this.backoffProperties = backoffProperties;
    }

    @Scheduled(fixedRateString = "${collector.fixeddelay}")
    public void runSchedule() {
        checkListenerList();
        setupConnectionsToNewNeighbours();
    }


    private void checkListenerList() {
        Set<String> remoteNames = listeners.keySet();
        for (String remoteName : remoteNames) {
            MessageCollectorListener listener = listeners.get(remoteName);
            if (! listener.isRunning()) {
                listeners.remove(remoteName);
                logger.info("Removed stopped listener {}", remoteName);
            }
        }

    }

    public void setupConnectionsToNewNeighbours() {
        List<ListenerEndpoint> listenerEndpoints = listenerEndpointRepository.findAll();
        List<String> interchangeNames = new ArrayList<>();

        for (ListenerEndpoint listenerEndpoint : listenerEndpoints) {
            String neighbourName = listenerEndpoint.getNeighbourName();
            interchangeNames.add(neighbourName);
            if (!listeners.containsKey(neighbourName)) {
                setUpConnectionToNeighbour(listenerEndpoint);
            }
            else {
                if (listeners.get(neighbourName).isRunning()) {
                    logger.debug("Listener for {} is still running with no changes", neighbourName);
                } else {
                    logger.debug("Non-running listener detected, name {}", neighbourName);
                }
            }

        }

        List<String> listenerKeysToRemove = new ArrayList<>();

        for (String ixnName : listeners.keySet()) {
            if (!interchangeNames.contains(ixnName)) {
                logger.info("Listener for {} is now being removed",ixnName);
                MessageCollectorListener toRemove = listeners.get(ixnName);
                logger.debug("Tearing down {}", ixnName);
                toRemove.teardown();
                listenerKeysToRemove.add(ixnName);
            }
        }

        listeners.keySet().removeAll(listenerKeysToRemove);
    }

    public void setUpConnectionToNeighbour(ListenerEndpoint listenerEndpoint){
        String name = listenerEndpoint.getNeighbourName();
        if(listenerEndpoint.getMessageConnection().canBeContacted(backoffProperties)) {
            try {
                logger.info("Setting up connection to ixn with name {}, URL {}", name, listenerEndpoint.getBrokerUrl());
                MessageCollectorListener messageListener = collectorCreator.setupCollection(listenerEndpoint);
                listeners.put(name, messageListener);
                listenerEndpoint.getMessageConnection().okConnection();
            } catch (MessageCollectorException e) {
                logger.warn("Tried to create connection to {}, but failed with exception.", name, e);
                listenerEndpoint.getMessageConnection().failedConnection(backoffProperties.getNumberOfAttempts());
            }
        }
        else {
            logger.info("Too soon to connect to {}", name);
        }
    }

    Map<String, MessageCollectorListener> getListeners() {
        return listeners;
    }
}
