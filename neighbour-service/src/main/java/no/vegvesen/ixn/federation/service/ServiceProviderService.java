package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@ConfigurationPropertiesScan
public class ServiceProviderService {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderService.class);

    private NeighbourRepository neighbourRepository;
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    public ServiceProviderService(NeighbourRepository neighbourRepository,
                                  ServiceProviderRepository serviceProviderRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }



    // hent ServiceProvidere som har LocalSubscriptions med createNewQueue og status ACCEPTED lokalt.
    //for hver ServiceProvider: finn naboen med OurRequestedSubscriptions, matche (subscriptionStatus = CREATED) og sette status CREATED og brokerUrl på LocalSubscription
    public void updateLocalSubscriptions(Self self) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        List<Neighbour> neighbours = neighbourRepository.findAll();
        for(ServiceProvider serviceProvider : serviceProviders) {
            updateServiceProviderSubscriptionsWithBrokerUrl(neighbours, serviceProvider, self.getMessageChannelUrl());
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void updateServiceProviderSubscriptionsWithBrokerUrl(List<Neighbour> neighbours, ServiceProvider serviceProvider, String localMessageBrokerUrl) {
        for(Neighbour neighbour : neighbours) {
            for(LocalSubscription localSubscription : serviceProvider.getSubscriptions()){
                for(Subscription subscription : neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions()){
                    if (localSubscription.getSelector().equals(subscription.getSelector())) {
                        if (localSubscription.isCreateNewQueue()) {
                            if (!localSubscription.getQueueConsumerUser().equals(subscription.getConsumerCommonName())) {
                                throw new IllegalStateException("createNewQueue requested, but subscription user is not the same as the local subscription user");
                            }
                            //TODO What about changes to brokers? We also write ALL service provider Brokers every time!
                            Set<Broker> brokers = subscription.getBrokers();
                            Set<LocalBroker> localBrokers = new HashSet<>();
                            for (Broker broker : brokers) {
                                logger.info("Adding local broker {} with createNewQueue true, queue {}", broker.getMessageBrokerUrl(), broker.getQueueName());
                                localBrokers.add(brokerToLocalBroker(broker));
                            }
                            serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, localBrokers);
                        } else {
                            if (localSubscription.getQueueConsumerUser().equals(subscription.getConsumerCommonName())) {
                                throw new IllegalStateException("createNewQueue = false, local subscription user = subscription user");
                            }
                            LocalBroker broker = new LocalBroker(serviceProvider.getName(), localMessageBrokerUrl);
                            logger.info("Adding local broker {} with createNewQueue false, queue {}", broker.getMessageBrokerUrl(), broker.getQueueName());
                            Set<LocalBroker> localBrokers = new HashSet<>(Arrays.asList(broker));
                            serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, localBrokers);
                        }
                    }
                }
            }
        }
    }

    public LocalBroker brokerToLocalBroker(Broker broker) {
        return new LocalBroker(broker.getQueueName(), broker.getMessageBrokerUrl());
    }
}
