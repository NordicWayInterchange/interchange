package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.model.Match;
import no.vegvesen.ixn.federation.model.MatchStatus;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
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
    private MatchDiscoveryService matchDiscoveryService;

    //NOTE: This is implicitly thread safe. If more than one thread can access the listeners map, the implementation of the listener Map will have to change.
    private Map<ListenerEndpoint, MessageCollectorListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageCollector.class);


    @Autowired
    public MessageCollector(ListenerEndpointRepository listenerEndpointRepository, CollectorCreator collectorCreator, GracefulBackoffProperties backoffProperties, MatchDiscoveryService matchDiscoveryService) {
        this.listenerEndpointRepository = listenerEndpointRepository;
        this.collectorCreator = collectorCreator;
        this.listeners = new HashMap<>();
        this.backoffProperties = backoffProperties;
        this.matchDiscoveryService = matchDiscoveryService;
    }

    @Scheduled(fixedRateString = "${collector.fixeddelay}")
    public void runSchedule() {
        checkListenerList();
        setupConnectionsToNewNeighbours();
    }


    private void checkListenerList() {
        Set<ListenerEndpoint> listenerEndpoints = listeners.keySet();
        for (ListenerEndpoint listenerEndpoint : listenerEndpoints) {
            MessageCollectorListener listener = listeners.get(listenerEndpoint);
            if (! listener.isRunning()) {
                listeners.remove(listenerEndpoint);
                logger.info("Removed stopped listener {} with host {} and port {}", listenerEndpoint.getNeighbourName(), listenerEndpoint.getHost(), listenerEndpoint.getPort());
            }
        }

    }

    public void setupConnectionsToNewNeighbours() {
        List<ListenerEndpoint> listenerEndpoints = listenerEndpointRepository.findAll();
        Set<ListenerEndpoint> interchangeListenerEndpoints = new HashSet<>();

        for (ListenerEndpoint listenerEndpoint : listenerEndpoints) {
            String neighbourName = listenerEndpoint.getNeighbourName();
            interchangeListenerEndpoints.add(listenerEndpoint);
            if (!listeners.containsKey(listenerEndpoint)) {
                Match match = matchDiscoveryService.findMatchesByExchangeName(listenerEndpoint.getExchangeName());
                if (match.getStatus().equals(MatchStatus.SETUP_ENDPOINT)) {
                    setUpConnectionToNeighbour(listenerEndpoint);
                    matchDiscoveryService.updateMatchToUp(match);
                }
            }
            else {
                if (listeners.get(listenerEndpoint).isRunning()) {
                    logger.debug("Listener for {} with host {} and port {} is still running with no changes", neighbourName, listenerEndpoint.getHost(), listenerEndpoint.getPort());
                } else {
                    logger.debug("Non-running listener detected, name {} with host {} and port {}", neighbourName, listenerEndpoint.getHost(), listenerEndpoint.getPort());
                }
            }

        }

        List<ListenerEndpoint> listenerKeysToRemove = new ArrayList<>();
        List<Match> tearDownEndpointMatches = matchDiscoveryService.findMatchesToTearDownEndpointsFor();
        //Removing matches that has not been properly removed
        Set<String> listenerEndpointExchanges = listeners.keySet().stream().map(ListenerEndpoint::getExchangeName).collect(Collectors.toSet());
        for (Match match : tearDownEndpointMatches) {
            if (!listenerEndpointExchanges.contains(match.getSubscription().getExchangeName())) {
                matchDiscoveryService.updateMatchToTearDownExchange(match);
            }
        }

        for (ListenerEndpoint listenerEndpoint : listeners.keySet()) {
            if (!interchangeListenerEndpoints.contains(listenerEndpoint)) {
                Match match = matchDiscoveryService.findMatchesByExchangeName(listenerEndpoint.getExchangeName());
                if (match.getStatus().equals(MatchStatus.TEARDOWN_ENDPOINT)) {
                    String neighbourName = listenerEndpoint.getNeighbourName();
                    logger.info("Listener for {} with host {} and port {} is now being removed", neighbourName, listenerEndpoint.getHost(), listenerEndpoint.getPort());
                    MessageCollectorListener toRemove = listeners.get(listenerEndpoint);
                    logger.debug("Tearing down listener for {} with host {} and port {}", neighbourName, listenerEndpoint.getHost(), listenerEndpoint.getPort());
                    toRemove.teardown();
                    listenerKeysToRemove.add(listenerEndpoint);
                    matchDiscoveryService.updateMatchToTearDownExchange(match);
                }
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
