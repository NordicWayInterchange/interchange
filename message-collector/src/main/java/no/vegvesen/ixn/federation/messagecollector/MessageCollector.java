package no.vegvesen.ixn.federation.messagecollector;

/*-
 * #%L
 * message-collector
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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

    //NOTE: This is implicitly thread safe. If more than one thread can access the listeners map, the implementation of the listener Map will have to change.
    private Map<String, MessageCollectorListener> listeners;
    private Logger logger = LoggerFactory.getLogger(MessageCollector.class);


    @Autowired
    public MessageCollector(NeighbourService service, CollectorCreator collectorCreator) {
        this.neighbourService = service;
        this.collectorCreator = collectorCreator;
        this.listeners = new HashMap<>();

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
                try {
                    logger.info("Setting up collection from ixn with name {}, port {}", ixn.getName(), ixn.getMessageChannelPort());
                    MessageCollectorListener messageListener = collectorCreator.setupCollection(ixn);
                    listeners.put(name, messageListener);
                } catch (MessageCollectorException e) {
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
