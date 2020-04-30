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
import java.util.stream.Collectors;

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
            Set<LocalSubscription> newSubscriptions = new HashSet<>();
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {

                Optional<LocalSubscription> newSubscription = processSubscription(groupMemberNames, name, subscription);
                newSubscription.ifPresent(localSubscription -> newSubscriptions.add(localSubscription));
            }
            //remove the queue and group member if we have no more subscriptions
            if (newSubscriptions.isEmpty()) {
                //note that qpidClient.queueExists will be called twice for a tear down of a serviceProvider.
                if (qpidClient.queueExists(name)) {
                    qpidClient.removeQueue(name);
                    logger.info("Removed queue for service provider {}", serviceProvider.getName());
                }
                if (groupMemberNames.contains(name)) {
                    logger.debug("Removing queue for service provider {}", serviceProvider.getName());
                    qpidClient.removeMemberFromGroup(name, SERVICE_PROVIDERS_GROUP_NAME);
                }
            }
            //save if it has changed from the initial
            if (! newSubscriptions.equals(serviceProvider.getSubscriptions())) {
                serviceProvider.setSubscriptions(newSubscriptions);
                repository.save(serviceProvider);
            }
        }
    }

    public Optional<LocalSubscription> processSubscription(List<String> groupMemberNames, String name, LocalSubscription subscription) {
        Optional<LocalSubscription> newSubscription;
        switch (subscription.getStatus()) {
            case REQUESTED:
                newSubscription = onRequested(groupMemberNames, name, subscription);
                break;
            case CREATED:
                newSubscription = onRequested(groupMemberNames, name, subscription);
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

    public Optional<LocalSubscription> onTearDown(String name, LocalSubscription subscription) {
        removeBindingIfExists(name, subscription);
        return Optional.empty();
    }

    private Optional<LocalSubscription> onRequested(List<String> groupMemberNames, String name, LocalSubscription subscription) {
        optionallyAddServiceProviderToGroup(groupMemberNames, name);
        optionallyCreateQueue(name);
        optionallyAddQueueBindings(name, subscription);
        return Optional.of(subscription.withStatus(LocalSubscriptionStatus.CREATED));
    }

    public void removeBindingIfExists(String name, LocalSubscription subscription) {
        if (qpidClient.queueExists(name)) {
            if (qpidClient.getQueueBindKeys(name).contains(subscription.bindKey())) {
                qpidClient.unbindBindKey(name, subscription.bindKey(), "nwEx");
                qpidClient.unbindBindKey(name, subscription.bindKey(), "fedEx");
            }
        }
    }

    public void optionallyAddQueueBindings(String name, LocalSubscription subscription) {
        if (!qpidClient.getQueueBindKeys(name).contains(subscription.bindKey())) {
            logger.debug("Adding bindings to the queue {}", name);
            qpidClient.addBinding(subscription.selector(), name, subscription.bindKey(), "nwEx");
            qpidClient.addBinding(subscription.selector(), name, subscription.bindKey(), "fedEx");
        }
    }

    public void optionallyCreateQueue(String name) {
        if (!qpidClient.queueExists(name)) {
            logger.info("Creating queue {}", name);
            qpidClient.createQueue(name);
            qpidClient.addReadAccess(name, name);
        }
    }

    public void optionallyAddServiceProviderToGroup(List<String> groupMemberNames, String name) {
        if (!groupMemberNames.contains(name)) {
            qpidClient.addMemberToGroup(name, SERVICE_PROVIDERS_GROUP_NAME);
        }
    }

}
