package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QpidService {


    private final OutgoingMatchRepository outgoingMatchRepository;

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

    public String joinTwoSelectors(String firstSelector, String secondSelector) {
        return String.format("(%s) AND (%s)", firstSelector, secondSelector);
    }

    public List<CapabilitiesLinkedDeliveryApi> getCapabilitiesLinkedDelivery(ServiceProvider serviceProvider, String deliveryId) {
        List<CapabilityMatchApi> capabilityMatches = new ArrayList<>();

        if (serviceProvider.hasDeliveries()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (delivery.getStatus().equals(LocalDeliveryStatus.CREATED)) {
                   List<OutgoingMatch> matches = outgoingMatchRepository.findAllByLocalDelivery_Uuid(deliveryId);
                    for (OutgoingMatch match : matches) {
                        Capability capability = match.getCapability();
                        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                            for (CapabilityShard shard : capability.getShards()) {
                                if (bindingExists(endpoint.getTarget(), shard.getExchangeName())) {
                                    String joinedSelector = joinTwoSelectors(shard.getSelector(), delivery.getSelector());
                                    Binding binding = new Binding(shard.getExchangeName(), endpoint.getTarget(), new Filter(joinedSelector));
                                    CapabilityMatchApi matchApi = new CapabilityMatchApi(
                                            capability.getUuid(),
                                            shard.getShardId(),
                                            binding
                                    );
                                    capabilityMatches.add(matchApi);
                                }
                            }
                        }
                    }
                }
            }
        }
        return toCapabilitiesLinkedDeliveryApi(capabilityMatches, deliveryId);
    }

    public List<CapabilitiesLinkedDeliveryApi> toCapabilitiesLinkedDeliveryApi(List<CapabilityMatchApi> matches, String deliveryId) {
        List<CapabilitiesLinkedDeliveryApi> result = new ArrayList<>();

        for (CapabilityMatchApi match : matches) {
            CapabilitiesLinkedDeliveryApi api = new CapabilitiesLinkedDeliveryApi(deliveryId, match);
            result.add(api);
        }
        return result;
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
