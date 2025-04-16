package no.vegvesen.ixn.federation.adminserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.vegvesen.ixn.federation.adminserver.model.endpoint.LocalDeliveryEndpointAdminApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilitiesLinkedDeliveryApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilityMatchApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityShardApi;
import no.vegvesen.ixn.federation.adminserver.model.shard.CapabilityShardAdminApi;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
                            endpoint.getTarget()
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

    public CapabilityApi capabilitiesMatchedDeliveryBasedOnCapabilityId(OutgoingMatch match) {
        Capability capability = match.getCapability();
        return new CapabilityApi(
                capability.getApplication().toApi(),
                capability.getMetadata().toApi(),
                capabilityShardSetToCapabilityShardSetApi(capability.getShards())
        );
    }

    public CapabilityShardAdminApi capabilitiesMatchedDeliveryBasedOnShardId(LocalDelivery delivery, OutgoingMatch match, String shardId) {
        Capability capability = match.getCapability();
        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
            String exchangeName = endpoint.getTarget();
            return new CapabilityShardAdminApi(new CapabilityShardIdApi(toCapabilityShardSetApi(capability.getShard(Integer.valueOf(shardId)))), exchangeName != null);
        }
        return null;
    }

    public Set<CapabilityShardApi> capabilityShardSetToCapabilityShardSetApi(List<CapabilityShard> capabilityShards) {
        Set<CapabilityShardApi> capabilityShardApiSet = new HashSet<>();
        for (CapabilityShard capabilityShard : capabilityShards) {
            capabilityShardApiSet.add(capabilityShardToCapabilityShardApi(capabilityShard));
        }
        return capabilityShardApiSet;
    }

    public Set<CapabilityShardApi> toCapabilityShardSetApi(Optional<CapabilityShard> capabilityShard) {
        Set<CapabilityShardApi> capabilityShardApiSet = new HashSet<>();

        capabilityShardApiSet.add(capabilityShardToCapabilityShardApi(capabilityShard));

        return capabilityShardApiSet;
    }

    public CapabilityShardApi capabilityShardToCapabilityShardApi(CapabilityShard capabilityShard) {
        return new CapabilityShardApi(
                capabilityShard.getShardId(),
                capabilityShard.getExchangeName(),
                capabilityShard.getSelector()
        );
    }

    public CapabilityShardApi capabilityShardToCapabilityShardApi(Optional<CapabilityShard> capabilityShard) {
        CapabilityShard shard = capabilityShard.orElseThrow(() -> new IllegalArgumentException("CapabilityShard is not present"));
        return new CapabilityShardApi(
                shard.getShardId(),
                shard.getExchangeName(),
                shard.getSelector()
        );
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
