package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.endpoint.LocalDeliveryEndpointAdminApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilitiesLinkedDeliveryApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilityMatchApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.LocalDeliveryEndpointApi;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.model.LocalDelivery;
import no.vegvesen.ixn.federation.model.LocalDeliveryEndpoint;
import no.vegvesen.ixn.federation.model.OutgoingMatch;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;

import java.util.ArrayList;
import java.util.List;

@Service
public class QpidService {

    private final AdminQpidClient adminQpidClient;

    @Autowired
    public QpidService(AdminQpidClient adminQpidClient) {
        this.adminQpidClient = adminQpidClient;
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

    public List<LocalDeliveryEndpointAdminApi> getLocalDeliveryEndpointApiList(LocalDelivery delivery) {
        List<LocalDeliveryEndpointAdminApi> result = new ArrayList<>();
        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
            Exchange exchange = adminQpidClient.getExchange(endpoint.getTarget());
            result.add(new LocalDeliveryEndpointAdminApi(
                    new LocalDeliveryEndpointApi(
                            endpoint.getHost(),
                            endpoint.getPort(),
                            endpoint.getTarget(),
                            endpoint.getDlqName()
                    ),
                    exchange != null
                    )
            );

        }
        return result;
    }

    public CapabilitiesLinkedDeliveryApi getCapabilitiesLinkedDelivery(LocalDelivery delivery, List<OutgoingMatch> matches) {
        String uuid = delivery.getUuid();
        List<CapabilityMatchApi> capabilityMatches = new ArrayList<>();
        for (OutgoingMatch match : matches) {
            Capability capability = match.getCapability();
            for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                for (CapabilityShard shard : capability.getShards()) {
                    boolean exists;
                    String exchangeName = endpoint.getTarget();
                    String queueName = shard.getExchangeName();
                    Exchange exchange = adminQpidClient.getExchange(exchangeName);
                    Binding binding;
                    if (exchange != null) {
                        binding = exchange.getBindingTo(queueName);
                        exists = binding != null;
                    } else {

                        String joinedSelector = joinTwoSelectors(shard.getSelector(), delivery.getSelector());
                        binding = new Binding(shard.getExchangeName(), endpoint.getTarget(), new Filter(joinedSelector));
                        exists = false;
                    }
                    CapabilityMatchApi matchApi = new CapabilityMatchApi(
                            capability.getUuid(),
                            shard.getShardId(),
                            binding,
                            exists
                    );
                    capabilityMatches.add(matchApi);
                }
            }
        }
        return new CapabilitiesLinkedDeliveryApi(uuid,capabilityMatches);
    }

    public List<Exchange> getAllExchanges() {
        try {
            return adminQpidClient.getAllExchanges();
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Queue> getAllQueues() {
        try {
            return adminQpidClient.getAllQueues();
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

}
