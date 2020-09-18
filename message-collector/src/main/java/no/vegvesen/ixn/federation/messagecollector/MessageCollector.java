package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.discoverer.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.Neighbour;
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

    //NOTE: This is implicitly thread safe. If more than one thread can access the listeners map, the implementation of the listener Map will have to change.
    private Map<String, MessageCollectorListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageCollector.class);


    @Autowired
    public MessageCollector(NeighbourService service, CollectorCreator collectorCreator, GracefulBackoffProperties backoffProperties) {
        this.neighbourService = service;
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
        List<Neighbour> interchanges = neighbourService.listNeighboursToConsumeMessagesFrom();
        List<String> interchangeNames = new ArrayList<>();
        for (Neighbour ixn : interchanges) {
            String name = ixn.getName();
            interchangeNames.add(name);
            if (! listeners.containsKey(name)) {
                if(backoffProperties.canBeContacted(ixn)) {
                    try {
                        logger.info("Setting up connection from ixn with name {}, port {}", ixn.getName(), ixn.getMessageChannelPort());
                        MessageCollectorListener messageListener = collectorCreator.setupCollection(ixn);
                        listeners.put(name, messageListener);
                    } catch (MessageCollectorException e) {
                        logger.warn("Tried to create connection to {}, but failed with exception.", name, e);
                    }
                }
                else {
                    logger.info("Too soon to set up connection to {}", name);
                }
            } else {
                if (listeners.get(name).isRunning()) {
                    logger.debug("Listener for {} is still running with no changes", name);
                } else {
                    logger.debug("Non-running listener detected, name {}",name);
                }
            }
        }
        List<String> listenerKeysToRemove = new ArrayList<>();
        for (String ixnName : listeners.keySet()) {
            if (! interchangeNames.contains(ixnName)) {
                logger.info("Listener for {} is now being removed",ixnName);

                MessageCollectorListener toRemove = listeners.get(ixnName);
                logger.info("Tearing down {}", ixnName);
                toRemove.teardown();
                listenerKeysToRemove.add(ixnName);
            }
        }
        for (String ixnName : listenerKeysToRemove) {
        	logger.debug("Removing {} from listeners", ixnName);
            listeners.remove(ixnName);
        }
    }

    Map<String, MessageCollectorListener> getListeners() {
        return listeners;
    }
}
