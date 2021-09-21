package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan
public class CapabilitiesService {

    private static Logger logger = LoggerFactory.getLogger(CapabilitiesService.class);

    private NeighbourRepository neighbourRepository;
    private CapabilitiesTransformer capabilitiesTransformer = new CapabilitiesTransformer();
    private DNSFacade dnsFacade;
    private InterchangeNodeProperties interchangeNodeProperties;
    private GracefulBackoffProperties backoffProperties;

    @Autowired
    public CapabilitiesService(NeighbourRepository neighbourRepository,
                               DNSFacade dnsFacade,
                               InterchangeNodeProperties interchangeNodeProperties,
                               GracefulBackoffProperties backoffProperties) {

        this.neighbourRepository = neighbourRepository;
        this.dnsFacade = dnsFacade;
        this.interchangeNodeProperties = interchangeNodeProperties;
        this.backoffProperties = backoffProperties;
    }

    public CapabilitiesApi incomingCapabilities(CapabilitiesApi neighbourCapabilities, Self self) {
        Capabilities incomingCapabilities = capabilitiesTransformer.capabilitiesApiToCapabilities(neighbourCapabilities);
        incomingCapabilities.setLastCapabilityExchange(LocalDateTime.now());

        logger.info("Looking up neighbour in DB.");
        Neighbour neighbourToUpdate = neighbourRepository.findByName(neighbourCapabilities.getName());

        if (neighbourToUpdate == null) {
            logger.info("*** CAPABILITY POST FROM NEW NEIGHBOUR ***");
            neighbourToUpdate = findNeighbour(neighbourCapabilities.getName());
        }
        logger.info("--- CAPABILITY POST FROM EXISTING NEIGHBOUR ---");
        neighbourToUpdate.setCapabilities(incomingCapabilities);

        logger.info("Saving updated Neighbour: {}", neighbourToUpdate.toString());
        neighbourRepository.save(neighbourToUpdate);

        return capabilitiesTransformer.selfToCapabilityApi(self);
    }
    public void capabilityExchangeWithNeighbours(Self self, NeighbourFacade neighbourFacade) {
        logger.info("Checking for any neighbours with UNKNOWN capabilities for capability exchange");
        List<Neighbour> neighboursForCapabilityExchange = neighbourRepository.findByCapabilities_StatusIn(
                Capabilities.CapabilitiesStatus.UNKNOWN,
                Capabilities.CapabilitiesStatus.KNOWN,
                Capabilities.CapabilitiesStatus.FAILED);
        capabilityExchange(neighboursForCapabilityExchange, self, neighbourFacade);
    }
    void capabilityExchange(List<Neighbour> neighboursForCapabilityExchange, Self self, NeighbourFacade neighbourFacade) {
        for (Neighbour neighbour : neighboursForCapabilityExchange) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                    if (neighbour.needsOurUpdatedCapabilities(self.getLastUpdatedLocalCapabilities())) {
                        logger.info("Posting capabilities to neighbour: {} ", neighbour.getName());
                        postCapabilities(self, neighbour, neighbourFacade);
                    } else {
                        logger.debug("Neighbour has our last capabilities");
                    }
                } else {
                    logger.info("Too soon to post capabilities to neighbour when backing off");
                }
            } catch (Exception e) {
                logger.error("Unknown exception while exchanging capabilities with neighbour", e);
            } finally {
                NeighbourMDCUtil.removeLogVariables();
            }
        }
    }

    private void postCapabilities(Self self, Neighbour neighbour, NeighbourFacade neighbourFacade) {
        try {
            Capabilities capabilities = neighbourFacade.postCapabilitiesToCapabilities(neighbour, self);
            capabilities.setLastCapabilityExchange(LocalDateTime.now());
            neighbour.setCapabilities(capabilities);
            neighbour.getControlConnection().okConnection();
            logger.info("Successfully completed capability exchange.");
            logger.debug("Updated neighbour: {}", neighbour.toString());
        } catch (CapabilityPostException e) {
            logger.error("Capability post failed", e);
            neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
            neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
        } finally {
            neighbour = neighbourRepository.save(neighbour);
            logger.info("Saving updated neighbour: {}", neighbour.toString());
        }
    }
    public void retryUnreachable(Self self, NeighbourFacade neighbourFacade) {
        List<Neighbour> unreachableNeighbours = neighbourRepository.findByControlConnection_ConnectionStatus(ConnectionStatus.UNREACHABLE);
        if (!unreachableNeighbours.isEmpty()) {
            logger.info("Retrying connection to unreachable neighbours {}", unreachableNeighbours.stream().map(Neighbour::getName).collect(Collectors.toList()));
            for (Neighbour neighbour : unreachableNeighbours) {
                try {
                    NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                    postCapabilities(self, neighbour, neighbourFacade);
                } catch (Exception e) {
                    logger.error("Error occurred while posting capabilities to unreachable neighbour", e);
                } finally {
                    NeighbourMDCUtil.removeLogVariables();
                }
            }
        }
    }

    Neighbour findNeighbour(String neighbourName) {
        return dnsFacade.lookupNeighbours().stream()
                .filter(n -> n.getName().equals(neighbourName))
                .findFirst()
                .orElseThrow(() ->
                        new InterchangeNotInDNSException(
                                String.format("Received capability post from neighbour %s, but could not find in DNS %s",
                                        neighbourName,
                                        dnsFacade.getDnsServerName()))
                );
    }

}
