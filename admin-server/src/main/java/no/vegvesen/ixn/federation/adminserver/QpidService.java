package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.qpid.AdminQpidDelta;
import no.vegvesen.ixn.federation.adminserver.qpid.Exchange;
import no.vegvesen.ixn.federation.adminserver.qpid.AdminQpidClient;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class QpidService {


    private final OutgoingMatchRepository outgoingMatchRepository;

    private final Logger logger = LoggerFactory.getLogger(QpidService.class);
    private final AdminQpidClient adminQpidClient;

    @Autowired
    public QpidService(AdminQpidClient adminQpidClient, OutgoingMatchRepository outgoingMatchRepository) {
        this.adminQpidClient = adminQpidClient;
        this.outgoingMatchRepository = outgoingMatchRepository;
    }

    public boolean exchangeExists(String exchangeName) {
        return adminQpidClient.exchangeExists(exchangeName);
    }

    public boolean queueExists(String queueName) {
        return adminQpidClient.queueExists(queueName);
    }

    public boolean bindingExists(String exchangeName, String queueName) {
        Exchange exchange = adminQpidClient.getExchange(exchangeName);
        if (exchange != null) {
            return exchange.isBoundToQueue(queueName);
        } else {
            return false;
        }
    }

    public CapabilityApi deliverysExchangeBindingToMatchingCapabilityExists(ServiceProvider serviceProvider, String deliveryId) {
        AdminQpidDelta delta = adminQpidClient.getQpidDelta();
        Integer intDeliveryId = null;
        try {
            intDeliveryId = Integer.valueOf(deliveryId);
        } catch (NumberFormatException e) {
            logger.error("Invalid deliver id: The ID must be a valid integer. Provided DeliveryId: {}", deliveryId, e);
        }
        if (serviceProvider.hasDeliveries()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (delivery.getStatus().equals(LocalDeliveryStatus.CREATED)) {
                   List<OutgoingMatch> matches = outgoingMatchRepository.findAllByLocalDelivery_Id(intDeliveryId);
                    for (OutgoingMatch match : matches) {
                        Capability capability = match.getCapability();
                        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                            for (CapabilityShard shard : capability.getShards()) {
                                if (delta.exchangeHasBindingToQueue(endpoint.getTarget(), shard.getExchangeName())) {
                                    return capabilityToCapabilitiesApiList(capability);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public CapabilityApi capabilityToCapabilitiesApiList(Capability capability) {
        return new CapabilityApi(
                capability.getUuid(),
                capability.getApplication().toApi(),
                capability.getMetadata().toApi(),
                localDateTimeToTimestamp(capability.getCreatedTimestamp())
        );
    }

    private Long localDateTimeToTimestamp(LocalDateTime lastUpdated) {
        Long epochSecond = null;
        if (lastUpdated != null) {
            epochSecond = lastUpdated.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return epochSecond;
    }

    public List<Exchange> getAllExchanges() {
        try {
            return adminQpidClient.getAllExchanges();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Queue> getAllQueues() {
        try {
            return adminQpidClient.getAllQueues();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
