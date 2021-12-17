package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
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
            updateServiceProvidersWithNonCreateNewQueue(serviceProvider,self.getMessageChannelUrl());
            updateServiceProvidersWithCreateNewQueue(neighbours, serviceProvider);
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void updateLocalDeliveries(Self self) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for(ServiceProvider serviceProvider : serviceProviders) {
            if (serviceProvider.hasCapabilities() && !serviceProvider.getDeliveries().isEmpty()) {
                for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                    if (delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)) {
                        boolean match = CapabilityMatcher.matchLocalDeliveryToServiceProviderCapabilities(serviceProvider.getCapabilities().getCapabilities(), delivery);
                        if (match) {
                            LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(
                                    self.getName(),
                                    Integer.parseInt(self.getMessageChannelPort()),
                                    "onramp",
                                    delivery.getSelector()
                            );
                            delivery.setEndpoints(Collections.singleton(endpoint));
                            delivery.setStatus(LocalDeliveryStatus.CREATED);
                        }
                    }
                }
            }
            serviceProviderRepository.save(serviceProvider);
        }
    }

    public void updateServiceProvidersWithCreateNewQueue(List<Neighbour> neighbours, ServiceProvider serviceProvider) {
        for(Neighbour neighbour : neighbours) {
            for(LocalSubscription localSubscription : serviceProvider.getSubscriptions()){
                for(Subscription subscription : neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions()){
                    if (localSubscription.getSelector().equals(subscription.getSelector())) {
                        if (localSubscription.isCreateNewQueue()) {
                            if (!localSubscription.getQueueConsumerUser().equals(serviceProvider.getName())) {
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
                        }
                    }
                }
            }
        }
    }

    public void updateServiceProvidersWithNonCreateNewQueue(ServiceProvider serviceProvider, String localMessageBrokerUrl) {
        for (LocalSubscription localSubscription : serviceProvider.getSubscriptions()) {
            if (!localSubscription.isCreateNewQueue()) {
                //TODO this is a bit strange. The local user sohuld be the sp!
                //if (localSubscription.getQueueConsumerUser().equals(serviceProvider.getName())) {
                //    throw new IllegalStateException("createNewQueue = false, local subscription user = subscription user");
                //}
                //To avoid overwriting CREATED subscriptions
                LocalBroker broker = new LocalBroker(serviceProvider.getName(), localMessageBrokerUrl);
                logger.info("Adding local broker {} with createNewQueue false, queue {}", broker.getMessageBrokerUrl(), broker.getQueueName());
                Set<LocalBroker> localBrokers = new HashSet<>(Arrays.asList(broker));
                serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, localBrokers);
            }
        }
    }

    public LocalBroker brokerToLocalBroker(Broker broker) {
        return new LocalBroker(broker.getQueueName(), broker.getMessageBrokerUrl());
    }
}
