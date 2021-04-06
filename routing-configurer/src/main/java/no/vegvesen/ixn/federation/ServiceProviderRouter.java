package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME;

@Component
public class ServiceProviderRouter {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouter.class);

    private final ServiceProviderRepository repository;
    private final QpidClient qpidClient;

    @Autowired
    public ServiceProviderRouter(ServiceProviderRepository repository, QpidClient qpidClient) {
        this.repository = repository;
        this.qpidClient = qpidClient;
    }


    public Iterable<ServiceProvider> findServiceProviders() {
		return repository.findAll();
    }

    public void syncServiceProviders(Iterable<ServiceProvider> serviceProviders) {
        List<String> groupMemberNames = qpidClient.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            logger.debug("Checking service provider {}",name);
            syncPrivateChannels(serviceProvider);
            Set<LocalSubscription> newSubscriptions = new HashSet<>();
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {

                Optional<LocalSubscription> newSubscription = processSubscription(name, subscription);
                newSubscription.ifPresent(newSubscriptions::add);
            }

            //making a set of LocalSubscriptions that has createNewQueue = true
            Set<LocalSubscription> hasCreateNewQueue = newSubscriptions
                    .stream()
                    .filter(LocalSubscription::isCreateNewQueue)
                    .collect(Collectors.toSet());

            //remove the queue and group member if we have no more subscriptions
            if (newSubscriptions.isEmpty()) {
                if (qpidClient.queueExists(name)) {
                    qpidClient.removeQueue(name);
                    logger.info("Removed queue for service provider {}", serviceProvider.getName());
                }
            }

            if (serviceProvider.hasCapabilitiesOrActiveSubscriptions()) {
                if (serviceProvider.hasCapabilities() || (newSubscriptions.size() > hasCreateNewQueue.size())) {
                    optionallyAddServiceProviderToGroup(groupMemberNames,name);
                }
            } else {
                if (groupMemberNames.contains(serviceProvider.getName())){
                    removeServiceProviderFromGroup(name,SERVICE_PROVIDERS_GROUP_NAME);
                }
            }

            //save if it has changed from the initial
            if (! newSubscriptions.equals(serviceProvider.getSubscriptions())) {
                serviceProvider.updateSubscriptions(newSubscriptions);
                repository.save(serviceProvider);
                logger.debug("Saved updated service provider {}",serviceProvider);
            } else {
                logger.debug("Service provider not changed, {}", serviceProvider);
            }
        }
    }

    private Optional<LocalSubscription> processSubscription(String name, LocalSubscription subscription) {
        Optional<LocalSubscription> newSubscription;
        switch (subscription.getStatus()) {
            case REQUESTED:
			case CREATED:
			    if (subscription.isCreateNewQueue()) {
                    newSubscription = Optional.of(subscription.withStatus(LocalSubscriptionStatus.CREATED));
                } else {
				    newSubscription = onRequested(name, subscription);
			    }
                break;
			case TEAR_DOWN:
                //	Check that the binding exist, if so, delete it
                newSubscription = onTearDown(name, subscription);
                break;
            default:
                throw new IllegalStateException("Unknown subscription status encountered");
        }
        return newSubscription;
    }

    private Optional<LocalSubscription> onTearDown(String name, LocalSubscription subscription) {
        removeBindingIfExists(name, subscription);
        return Optional.empty();
    }

    private Optional<LocalSubscription> onRequested(String name, LocalSubscription subscription) {
        optionallyCreateQueue(name);
        optionallyAddQueueBindings(name, subscription);
        return Optional.of(subscription.withStatus(LocalSubscriptionStatus.CREATED));
    }

    private void removeBindingIfExists(String name, LocalSubscription subscription) {
        if (qpidClient.queueExists(name)) {
            if (qpidClient.getQueueBindKeys(name).contains(subscription.bindKey())) {
                qpidClient.unbindBindKey(name, subscription.bindKey(), "nwEx");
                qpidClient.unbindBindKey(name, subscription.bindKey(), "fedEx");
            }
        }
    }

    private void optionallyAddQueueBindings(String name, LocalSubscription subscription) {
        if (!qpidClient.getQueueBindKeys(name).contains(subscription.bindKey())) {
            logger.debug("Adding bindings to the queue {}", name);
            qpidClient.addBinding(subscription.getSelector(), name, subscription.bindKey(), "nwEx");
            qpidClient.addBinding(subscription.getSelector(), name, subscription.bindKey(), "fedEx");
        }
    }

    private void optionallyCreateQueue(String name) {
        if (!qpidClient.queueExists(name)) {
            logger.info("Creating queue {}", name);
            qpidClient.createQueue(name);
            qpidClient.addReadAccess(name, name);
        }
    }

    private void optionallyAddServiceProviderToGroup(List<String> groupMemberNames, String name) {
        if (!groupMemberNames.contains(name)) {
            logger.debug("Adding member {} to group {}", name,SERVICE_PROVIDERS_GROUP_NAME);
            qpidClient.addMemberToGroup(name, SERVICE_PROVIDERS_GROUP_NAME);
        }
    }

    private void removeServiceProviderFromGroup(String name, String groupName) {
        logger.debug("Removing member {} from group {}", name,groupName);
        qpidClient.removeMemberFromGroup(name,groupName);
    }

    public void syncPrivateChannels(ServiceProvider serviceProvider) {
        List<String> groupMemberNames = qpidClient.getGroupMemberNames(CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
        if(!serviceProvider.getPrivateChannels().isEmpty()) {
            Set<PrivateChannel> privateChannelsWithStatusCreated = serviceProvider.getPrivateChannels()
                    .stream()
                    .filter(s -> s.getStatus().equals(PrivateChannelStatus.CREATED))
                    .collect(Collectors.toSet());

            if(!groupMemberNames.contains(serviceProvider.getName())) {
                qpidClient.addMemberToGroup(serviceProvider.getName(), CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                logger.debug("Adding member {} to group {}", serviceProvider.getName(), CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
            }

            for(PrivateChannel privateChannel : serviceProvider.getPrivateChannels()) {
                String clientName = privateChannel.getClientName();
                String queueName = privateChannel.getQueueName();

                if(privateChannel.getStatus().equals(PrivateChannelStatus.REQUESTED)) {
                    qpidClient.addMemberToGroup(clientName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    logger.debug("Adding member {} to group {}", clientName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    qpidClient.createQueue(queueName);
                    logger.info("Creating queue {}", queueName);
                    qpidClient.addWriteAccess(serviceProvider.getName(), queueName);
                    qpidClient.addWriteAccess(clientName, queueName);
                    qpidClient.addReadAccess(serviceProvider.getName(), queueName);
                    qpidClient.addReadAccess(clientName, queueName);
                    privateChannel.setStatus(PrivateChannelStatus.CREATED);
                    logger.info("Creating queue {} for client {}", queueName, clientName);
                    repository.save(serviceProvider);
                }
                if(privateChannel.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)) {
                    if(groupMemberNames.contains(serviceProvider.getName()) && privateChannelsWithStatusCreated.isEmpty()){
                        qpidClient.removeMemberFromGroup(serviceProvider.getName(), CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                        logger.debug("Removing member {} from group {}", serviceProvider.getName(), CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    }
                    qpidClient.removeMemberFromGroup(clientName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    logger.info("Removing member {} from group {}", clientName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    qpidClient.removeWriteAccess(clientName, queueName);
                    qpidClient.removeWriteAccess(serviceProvider.getName(), queueName);
                    qpidClient.removeReadAccess(clientName, queueName);
                    qpidClient.removeReadAccess(serviceProvider.getName(), queueName);
                    logger.info("Tearing down queue {} for client {}", queueName, clientName);
                    qpidClient.removeQueue(queueName);
                    repository.save(serviceProvider);
                }
            }
            Set<PrivateChannel> privateChannelsToRemove = serviceProvider.getPrivateChannels()
                    .stream()
                    .filter(s -> s.getStatus().equals(PrivateChannelStatus.TEAR_DOWN))
                    .collect(Collectors.toSet());

            serviceProvider.getPrivateChannels().removeAll(privateChannelsToRemove);
            repository.save(serviceProvider);
        }
    }

}
