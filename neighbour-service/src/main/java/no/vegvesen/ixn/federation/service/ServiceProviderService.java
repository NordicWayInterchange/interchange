package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
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
            for(Neighbour neighbour : neighbours) {
                for(LocalSubscription localSubscription : serviceProvider.getSubscriptions()){
                    for(Subscription subscription : neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions()){
                        if(localSubscription.getSelector().equals(subscription.getSelector()) &&
                            localSubscription.getQueueConsumerUser().equals(subscription.getQueueConsumerUser()) &&
                            localSubscription.isCreateNewQueue()){
                            //if(subscription.getBrokerUrl() != null){
                                //serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, subscription.getBrokerUrl());
                            //}
                            //TODO assume only one broker
                            Set<Broker> brokers = subscription.getBrokers();
                            Set<LocalBroker> localBrokers = new HashSet<>();
                            for(Broker broker : brokers){
                                localBrokers.add(brokerToLocalBroker(broker));
                            }
                            serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, localBrokers);
                        }
                        if(localSubscription.getSelector().equals(subscription.getSelector()) && !localSubscription.isCreateNewQueue()){
                            Set<LocalBroker> localBrokers = new HashSet<>(Arrays.asList(new LocalBroker(serviceProvider.getName(), self.getMessageChannelUrl())));
                            serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, localBrokers);
                        }
                    }
                }
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public LocalBroker brokerToLocalBroker(Broker broker) {
        return new LocalBroker(broker.getQueueName(), broker.getMessageBrokerUrl());
    }
}
