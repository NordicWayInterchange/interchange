package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class MessageCollector {

    private NeighbourFetcher neighbourFetcher;
    private final CollectorCreator collectorCreator;
    private Map<String, MessageCollectorListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageCollector.class);


    @Autowired
    public MessageCollector(NeighbourFetcher fetcher, CollectorCreator collectorCreator) {
        this.neighbourFetcher = fetcher;
        this.collectorCreator = collectorCreator;
        this.listeners = new HashMap<>();

    }

    @Scheduled(fixedRate = 30000)
    public void runSchedule() throws NamingException {
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

    public void setupConnectionsToNewNeighbours() throws NamingException {
        List<Neighbour> interchanges = neighbourFetcher.listNeighboursToConsumeFrom();
        List<String> interchangeNames = new ArrayList<>();
        for (Neighbour ixn : interchanges) {
            String name = ixn.getName();
            interchangeNames.add(name);
            if (! listeners.containsKey(name)) {
                try {
                    logger.info("Setting up ixn with name {}, port {}", ixn.getName(), ixn.getMessageChannelPort());
                    //MessageCollectorListener messageListener = collectorCreator.setupForwarding(ixn);
                    MessageCollectorListener messageListener = collectorCreator.setupCollection(ixn);
                    listeners.put(name, messageListener);
                } catch (JMSException e) {
                    logger.warn("Tried to create connection to {}, but failed with exception.",name,e);
                }
            } else {
                if (listeners.get(name).isRunning()) {
                    logger.debug("Listener for {} is still running with no changes", name);
                } else {
                    logger.debug("Non-running listener detected, name {}",name);
                }
            }
        }
        //TODO need a better way of doing this. Not even close to thread-safe..
        List<MessageCollectorListener> listenersToRemove = new ArrayList<>();
        for (String ixnName : listeners.keySet()) {
            if (! interchangeNames.contains(ixnName)) {
                logger.info("Listener for {} is now being removed",ixnName);
                MessageCollectorListener toRemove = listeners.get(ixnName);
                toRemove.teardown();
                listenersToRemove.add(toRemove);
            }
        }
        for (MessageCollectorListener listener : listenersToRemove) {
            listeners.remove(listener);
        }
    }

    Map<String, MessageCollectorListener> getListeners() {
        return listeners;
    }
}
