package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
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

import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;

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

}
