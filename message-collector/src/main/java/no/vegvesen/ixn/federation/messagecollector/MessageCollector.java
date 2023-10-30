package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
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
    private NeighbourRepository neighbourRepository;
    //NOTE: This is implicitly thread safe. If more than one thread can access the listeners map, the implementation of the listener Map will have to change.
    private Map<Endpoint, MessageCollectorListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageCollector.class);


    @Autowired
    public MessageCollector(NeighbourRepository neighbourRepository, CollectorCreator collectorCreator) {
        this(neighbourRepository,collectorCreator,new HashMap<>());
    }

    public MessageCollector(NeighbourRepository neighbourRepository, CollectorCreator collectorCreator, Map<Endpoint,MessageCollectorListener> listeners) {
        this.neighbourRepository = neighbourRepository;
        this.collectorCreator = collectorCreator;
        this.listeners = listeners;
    }

    @Scheduled(fixedRateString = "${collector.fixeddelay}")
    public void runSchedule() {
        checkListenerList();
        setupConnectionsToNewNeighbours();
    }

    public void checkListenerList() {
        //TODO logging of the entries...
        List<Endpoint> entriesToRemove = listeners.entrySet().stream().filter(e -> !e.getValue().isRunning()).map(e -> e.getKey()).collect(Collectors.toList());
        listeners.keySet().removeAll(entriesToRemove);
        logger.info("Removed listeners {}", entriesToRemove);
    }

    public void setupConnectionsToNewNeighbours() {
        List<Neighbour> neighbours = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.CREATED);
        Set<Endpoint> endpoints = new HashSet<>();
        for (Neighbour neighbour : neighbours) {
            for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)) {
                    endpoints.addAll(subscription.getEndpoints());
                }
            }
        }

        Set<Endpoint> interchangeEndpoints = new HashSet<>();

        for (Endpoint endpoint : endpoints) {
            String hostName = endpoint.getHost();
            interchangeEndpoints.add(endpoint);

            if (!listeners.containsKey(endpoint)) {
                setUpConnectionToNeighbour(endpoint);
            } else {
                if (listeners.get(endpoint).isRunning()) {
                    logger.debug("Listener with host {} is still running with no changes", hostName);
                } else {
                    logger.debug("Non-running listener detected, with host {}", hostName);
                }
            }
        }

        List<Endpoint> listenerKeysToRemove = new ArrayList<>();

        for (Endpoint endpoint : listeners.keySet()) {
            if (!interchangeEndpoints.contains(endpoint)) {
                logger.info("Listener with host {} and port {} is now being removed", endpoint.getHost(), endpoint.getPort());
                MessageCollectorListener toRemove = listeners.get(endpoint);
                logger.debug("Tearing down listener with host {} and port {}", endpoint.getHost(), endpoint.getPort());
                toRemove.teardown();
                listenerKeysToRemove.add(endpoint);
            }
        }

        listeners.keySet().removeAll(listenerKeysToRemove);
    }

    public void setUpConnectionToNeighbour(Endpoint endpoint){
        String name = endpoint.getHost();
        try {
            logger.info("Setting up connection to ixn with name {}, host {} and port {}", name, endpoint.getHost(), endpoint.getPort());
            MessageCollectorListener messageListener = collectorCreator.setupCollection(endpoint);
            listeners.put(endpoint, messageListener);
        } catch (MessageCollectorException e) {
            logger.warn("Tried to create connection to {} with host {} and port {}, but failed with exception.", name, endpoint.getHost(), endpoint.getPort(), e);
        }
    }
    Map<Endpoint, MessageCollectorListener> getListeners() {
        return listeners;
    }
}
