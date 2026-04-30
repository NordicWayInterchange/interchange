package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.federation.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import no.vegvesen.ixn.federation.service.routing.localdelivery.LocalDeliveryService;
import no.vegvesen.ixn.federation.service.routing.localsubscription.LocalSubscriptionService;
import no.vegvesen.ixn.shared.properties.CapabilityMessageTypeQueueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan("no.vegvesen.ixn")
public class ServiceProviderRouter {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderRouter.class);

    private final ServiceProviderRepository repository;

    private final PrivateChannelRepository privateChannelRepository;

    private final QpidClient qpidClient;

    private final InterchangeNodeProperties nodeProperties;

    private final LocalDeliveryService localDeliveryService;

    private final LocalSubscriptionService localSubscriptionService;

    private final OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    @Autowired
    public ServiceProviderRouter(ServiceProviderRepository repository, PrivateChannelRepository privateChannelRepository, QpidClient qpidClient, LocalSubscriptionService localSubscriptionService, InterchangeNodeProperties nodeProperties, LocalDeliveryService localDeliveryService, OutgoingMatchDiscoveryService outgoingMatchDiscoveryService) {
        this.repository = repository;
        this.privateChannelRepository = privateChannelRepository;
        this.qpidClient = qpidClient;
        this.nodeProperties = nodeProperties;
        this.localDeliveryService = localDeliveryService;
        this.localSubscriptionService = localSubscriptionService;
        this.outgoingMatchDiscoveryService = outgoingMatchDiscoveryService;

    }

    @Scheduled(fixedRateString = "${service-provider-router.interval}")
    public void checkForServiceProvidersToSetupRoutingFor() {
        logger.debug("Checking for new service providers to setup routing");
        Iterable<ServiceProvider> serviceProviders = repository.findAll();
        syncServiceProviders(serviceProviders, qpidClient.getQpidDelta());
    }


    @Scheduled(fixedRateString = "${routing-configurer.match-update-interval}", initialDelayString = "${routing-configurer.local-subscription-initial-delay}")
    public void createOutgoingMatches() {
        outgoingMatchDiscoveryService.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(repository.findAll());
    }

    @Scheduled(fixedRateString = "${routing-configurer.match-update-interval}", initialDelayString = "${routing-configurer.local-subscription-initial-delay}")
    public void updateOutgoingMatchesToTearDown() {
        outgoingMatchDiscoveryService.syncOutgoingMatchesToDelete();
    }

    public void syncServiceProviders(Iterable<ServiceProvider> serviceProviders, QpidDelta delta) {
        String messageChannelPort = nodeProperties.getMessageChannelPort();
        String brokerExternalName = nodeProperties.getBrokerExternalName();
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            logger.debug("Checking service provider {}", name);

            localDeliveryService.removeTearDownIllegalAndErrorDeliveries(serviceProvider);
            addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider, delta);
            syncPrivateChannels(serviceProvider, delta);
            serviceProvider = localDeliveryService.tearDownDeliveryQueues(serviceProvider, delta);
            serviceProvider = tearDownCapabilityExchanges(serviceProvider, delta);
            serviceProvider = localSubscriptionService.syncSubscriptions(brokerExternalName, messageChannelPort, serviceProvider, delta);
            serviceProvider = localSubscriptionService.removeUnwantedSubscriptions(serviceProvider);

            ServiceProviderMember groupMember = qpidClient.getServiceProviderMember(serviceProvider.getName());
            if (serviceProvider.hasCapabilitiesOrActiveSubscriptions()) {
                if (groupMember == null) {
                    qpidClient.addServiceProviderMemberToGroup(serviceProvider.getName());
                }
            } else {
                if (groupMember != null) {
                    qpidClient.removeServiceProviderMemberFromGroup(groupMember);
                }
            }

            serviceProvider = setUpCapabilityExchanges(serviceProvider, delta);
            bindCapabilityExchangesToBiQueue(serviceProvider, delta);
            serviceProvider = localSubscriptionService.syncLocalSubscriptionsToServiceProviderCapabilities(serviceProvider, delta, serviceProviders);
            localDeliveryService.updateDeliveryStatus(brokerExternalName, Integer.parseInt(messageChannelPort), serviceProvider);
            localDeliveryService.setUpDeliveryQueue(serviceProvider, delta);
        }
    }

    public void addOrRemoveServiceProviderToBiConsumerGroup(ServiceProvider serviceProvider, QpidDelta delta) {
        BiConsumerMember biConsumerMember = delta.findBiConsumerMemberByName(serviceProvider.getName());
        if (Boolean.TRUE.equals(serviceProvider.isBiconsumer())) {
            if (biConsumerMember == null) {
                biConsumerMember = qpidClient.addBiConsumerMemberToGroup(serviceProvider.getName());
                delta.addBiConsumerMember(biConsumerMember);
            }
        } else {
            if (biConsumerMember != null) {
                qpidClient.removeBiConsumerMemberFromGroup(biConsumerMember);
                delta.removeBiConsumerMember(biConsumerMember);
            }
        }
    }

    public void syncPrivateChannels(ServiceProvider serviceProvider, QpidDelta delta) {
        List<PrivateChannel> privateChannelList = privateChannelRepository.findAllByServiceProviderName(serviceProvider.getName());
        if (!privateChannelList.isEmpty()) {
            syncPrivateChannelsWithQpid(privateChannelList, serviceProvider.getName(), delta);
            privateChannelList.stream().filter((a) -> a.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)).forEach(privateChannelRepository::delete);
        }
    }

    private void syncPrivateChannelsWithQpid(List<PrivateChannel> privateChannels, String name, QpidDelta delta) {
        List<PrivateChannel> privateChannelsWithStatusCreated = privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, name);
        PrivateChannelMember privateChannelUser = delta.findByPrivateChannelUserName(name);
        if (privateChannelUser == null) {
            privateChannelUser = qpidClient.addPrivateChannelMemberToGroup(name);
            delta.addPrivateChannelUser(privateChannelUser);
            logger.debug("Adding member {} to private channel group", name);
        }

        for (PrivateChannel privateChannel : privateChannels) {
            String queueName = privateChannel.getEndpoint().getQueueName();

            if (privateChannel.getStatus().equals(PrivateChannelStatus.REQUESTED)) {
                Queue queue = delta.findByQueueName(queueName);
                if (queue == null) {
                    queue = qpidClient.createNonDestructiveQueue(queueName);
                    delta.addQueue(queue);
                }
                logger.info("Creating queue {}", queueName);
                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                provider.addExchangeWriteAccess(name, queueName);
                provider.addQueueReadAccess(name, queueName);
                for (Peer peer : privateChannel.getPeers()) {
                    String peerName = peer.getName();
                    PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                    if (peerMember == null) {
                        peerMember = qpidClient.addPrivateChannelMemberToGroup(peerName);
                        logger.debug("Adding member {} to private channel group", peer.getName());
                    }
                    peer.setStatus(PeerStatus.CREATED);
                    provider.addQueueReadAccess(peer.getName(), queueName);
                }
                qpidClient.postQpidAcl(provider);
                privateChannel.setStatus(PrivateChannelStatus.CREATED);
                logger.info("Creating private channel {} for client {}", queueName, name);
                privateChannel.setLastUpdated(LocalDateTime.now());
                privateChannelRepository.save(privateChannel);
            }
            if (privateChannel.getStatus().equals(PrivateChannelStatus.CREATED)) {
                Set<Peer> requestedPeers = privateChannel.getPeers().stream().filter(peer -> peer.getStatus().equals(PeerStatus.REQUESTED)).collect(Collectors.toSet());
                if (!requestedPeers.isEmpty()) {
                    VirtualHostAccessController provider = qpidClient.getQpidAcl();
                    for (Peer peer : requestedPeers) {
                        String peerName = peer.getName();
                        PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                        if (peerMember == null) {
                            peerMember = qpidClient.addPrivateChannelMemberToGroup(peerName);
                            delta.addPrivateChannelUser(peerMember);
                            logger.debug("Adding member {} to private channel group", peer.getName());
                        }
                        peer.setStatus(PeerStatus.CREATED);
                        provider.addQueueReadAccess(peer.getName(), queueName);
                        privateChannel.setLastUpdated(LocalDateTime.now());
                        privateChannelRepository.save(privateChannel);
                    }
                }

                Set<Peer> peersToRemove = privateChannel.getPeers().stream().filter(peer -> peer.getStatus().equals(PeerStatus.TEAR_DOWN)).collect(Collectors.toSet());
                if (!peersToRemove.isEmpty()) {
                    VirtualHostAccessController provider = qpidClient.getQpidAcl();
                    for (Peer peer : peersToRemove) {
                        String peerName = peer.getName();
                        long channelsWithPeerAsPeer = privateChannelRepository.findAllByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED).size();
                        long channelsWithPeerAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(peerName, PrivateChannelStatus.CREATED);

                        if (channelsWithPeerAsPeer <= 1 && channelsWithPeerAsServiceProvider == 0) {
                            PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                            if (peerMember != null) {
                                qpidClient.removePrivateChannelMemberFromGroup(peerMember);
                                delta.removePrivateChannelUser(peerMember);
                                logger.info("Adding member {} to private channel group", peerName);
                            }
                        }
                        provider.removeQueueReadAccess(peerName, queueName);
                    }
                    privateChannel.removePeers(peersToRemove);
                    privateChannel.setLastUpdated(LocalDateTime.now());
                    privateChannelRepository.save(privateChannel);
                }
            }

            if (privateChannel.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)) {
                long channelsWithServiceProviderAsPeer = privateChannelRepository.findAllByPeerNameAndStatus(privateChannel.getServiceProviderName(), PrivateChannelStatus.CREATED).size();
                long channelsWithServiceProviderAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(privateChannel.getServiceProviderName(), PrivateChannelStatus.CREATED);

                if (channelsWithServiceProviderAsServiceProvider == 0 && channelsWithServiceProviderAsPeer == 0) {
                    PrivateChannelMember groupMember = delta.findByPrivateChannelUserName(name);
                    if (groupMember != null && privateChannelsWithStatusCreated.isEmpty()) {
                        qpidClient.removePrivateChannelMemberFromGroup(groupMember);
                        delta.removePrivateChannelUser(groupMember);
                        logger.debug("Adding member {} to private channel group", name);
                    }
                }

                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                for (Peer peer : privateChannel.getPeers()) {
                    String peerName = peer.getName();
                    long channelsWithPeerAsPeer = privateChannelRepository.findAllByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED).size();
                    long channelsWithPeerAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(peerName, PrivateChannelStatus.CREATED);

                    if (channelsWithPeerAsPeer == 0 && channelsWithPeerAsServiceProvider == 0) {
                        PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                        if (peerMember != null) {
                            qpidClient.removePrivateChannelMemberFromGroup(peerMember);
                            delta.removePrivateChannelUser(peerMember);
                            logger.info("Adding member {} to private channel group", peerName);
                        }
                    }
                    provider.removeQueueReadAccess(peerName, queueName);
                }
                provider.removeQueueWriteAccess(name, queueName);
                provider.removeQueueReadAccess(name, queueName);
                qpidClient.postQpidAcl(provider);
                logger.info("Tearing down queue {} for client {}", queueName, name);
                Queue queue = delta.findByQueueName(queueName);
                if (queue != null) {
                    qpidClient.removeQueue(queue);
                    delta.removeQueue(queue);
                }
                privateChannel.setLastUpdated(LocalDateTime.now());
                privateChannelRepository.save(privateChannel);
            }
        }
    }

    public ServiceProvider setUpCapabilityExchanges(ServiceProvider serviceProvider, QpidDelta delta) {
        Set<Capability> requestedCaps = serviceProvider.getCapabilities().getCapabilitiesByStatusIsNot(CapabilityStatus.TEAR_DOWN);
        for (Capability capability : requestedCaps) {
            if (!capability.hasShards()) {
                List<CapabilityShard> newShards = new ArrayList<>();
                int numberOfShards = capability.getMetadata().getShardCount();
                for (int i = 0; i < numberOfShards; i++) {
                    String exchangeName = "cap-" + UUID.randomUUID();
                    Exchange exchange = qpidClient.createHeadersExchange(exchangeName);
                    logger.info("Created exchange {} for Capability with id {}", exchangeName, capability.getId());
                    delta.addExchange(exchange);

                    String capabilitySelector;
                    if (capability.isSharded()) {
                        capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability, i+1);
                    } else {
                        capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability, null);
                    }
                    CapabilityShard newShard = new CapabilityShard(i + 1, exchangeName, capabilitySelector);
                    newShards.add(newShard);
                }
                capability.setShards(newShards);
                capability.setStatus(CapabilityStatus.CREATED);
            } else {
                for (CapabilityShard shard : capability.getShards()) {
                    Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
                    if (exchange == null) {
                        exchange = qpidClient.createHeadersExchange(shard.getExchangeName());
                        delta.addExchange(exchange);
                    }
                }
                capability.setStatus(CapabilityStatus.CREATED);
            }
        }
        return repository.save(serviceProvider);
    }

    public void bindCapabilityExchangesToBiQueue(ServiceProvider serviceProvider, QpidDelta delta) {
        for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
            String messageType = capability.getApplication().getMessageType();
            String queueName = CapabilityMessageTypeQueueMapper.MESSAGE_TYPE_TO_QUEUE.get(messageType);
            for (CapabilityShard shard : capability.getShards()) {
                Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
                if (exchange != null) {
                    if (!exchange.isBoundTo(queueName)) {
                        Binding binding = new Binding(shard.getExchangeName(), queueName, new Filter(shard.getSelector()));
                        qpidClient.addBinding(shard.getExchangeName(), binding);
                        exchange.addBinding(binding);
                    }
                } else {
                    logger.info("Could not bind capability {}, shard with exchange name {} to bi-queue, exchange does not exist", capability.getUuid(), shard.getExchangeName());
                }
            }
        }
    }

    public ServiceProvider tearDownCapabilityExchanges(ServiceProvider serviceProvider, QpidDelta delta) {
        Set<Capability> tearDownCapabilities = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(capability -> capability.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .collect(Collectors.toSet());

        if (!tearDownCapabilities.isEmpty()) {
            for (Capability capability : tearDownCapabilities) {
                if (capability.hasShards()) {
                    for (CapabilityShard shard : capability.getShards()) {
                        Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
                        if (exchange != null) {
                            qpidClient.removeExchange(exchange);
                            logger.info("Removed exchange {} for Capability with id {}", shard.getExchangeName(), capability.getId());
                            delta.removeExchange(exchange);
                        }
                    }
                    capability.removeShards();
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

}
