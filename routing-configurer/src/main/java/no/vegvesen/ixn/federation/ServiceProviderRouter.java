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
            //Set<String> existingBindKeys = qpidClient.getQueueBindKeys(serviceProvider.getName());
            Set<LocalSubscription> subscriptionsToRemove = new HashSet<>();
            String name = serviceProvider.getName();
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
                switch (subscription.getStatus()) {
                    case REQUESTED:
                        //	Check whether or not the user exists, if not, create it
                        setupServiceProviderRouting(groupMemberNames, name, subscription);
                        subscription.setStatus(LocalSubscriptionStatus.CREATED);
                        repository.save(serviceProvider);
                        break;
                    case CREATED:
                        //	Check whether or not the user exists, if not, create it
                        setupServiceProviderRouting(groupMemberNames, name, subscription);
                        break;
                    case TEAR_DOWN:
                        //	Check that the binding exist, if so, delete it
                        if (qpidClient.queueExists(name)) {
                            if (qpidClient.getQueueBindKeys(name).contains(subscription.bindKey())) {
                                qpidClient.unbindBindKey(name, subscription.bindKey(), "nwEx");
                                qpidClient.unbindBindKey(name, subscription.bindKey(), "fedEx");
                            }
                        }
                        //	signal that the subscriptions should be removed from the service provider
                        subscriptionsToRemove.add(subscription);
                        break;
                    default:
                        throw new IllegalStateException("Unknown subscription status encountered");
                }
            }
            if (!subscriptionsToRemove.isEmpty()) {
                serviceProvider.getSubscriptions().removeAll(subscriptionsToRemove);
                logger.debug("Removed one or more subscriptions");
                repository.save(serviceProvider);
            }
            if (serviceProvider.getSubscriptions().isEmpty()) {
                if (qpidClient.queueExists(name)) {
                    qpidClient.removeQueue(name);
                }
                if (groupMemberNames.contains(name)) {
                    qpidClient.removeMemberFromGroup(name, SERVICE_PROVIDERS_GROUP_NAME);
                }
            }
        }
    }

    public void setupServiceProviderRouting(List<String> groupMemberNames, String name, LocalSubscription subscription) {
        if (!groupMemberNames.contains(name)) {
            qpidClient.addMemberToGroup(name, SERVICE_PROVIDERS_GROUP_NAME);
        }
        //	create the queue
        if (!qpidClient.queueExists(name)) {
            qpidClient.createQueue(name);
            qpidClient.addReadAccess(name, name);
            qpidClient.addBinding(subscription.selector(), name, subscription.bindKey(), "nwEx");
            qpidClient.addBinding(subscription.selector(), name, subscription.bindKey(), "fedEx");

        }
    }

}
