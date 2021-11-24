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

    public void updateLocalSubscriptions(Self self) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        List<Neighbour> neighbours = neighbourRepository.findAll();
        for(ServiceProvider serviceProvider : serviceProviders) {
            updateServiceProviderSubscriptionsWithBrokerUrl(neighbours, serviceProvider, self.getMessageChannelUrl());
            serviceProviderRepository.save(serviceProvider);
        }
    }
    //TODO: make test for this method
    public void updateServiceProviderSubscriptionsWithBrokerUrl(List<Neighbour> neighbours, ServiceProvider serviceProvider, String localMessageBrokerUrl) {
        for(Neighbour neighbour : neighbours) {
            for(LocalSubscription localSubscription : serviceProvider.getSubscriptions()){
                for(Subscription subscription : neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions()){
                    if (localSubscription.getSelector().equals(subscription.getSelector())) {
                        if (localSubscription.getConsumerCommonName().equals(serviceProvider.getName())) {
                            //TODO What about changes to endpoints? We also write ALL service provider Endpoints every time!
                            Set<Endpoint> endpoints = subscription.getEndpoints();
                            Set<LocalEndpoint> localEndpoints = new HashSet<>();
                            for (Endpoint endpoint : endpoints) {
                                logger.info("Adding local endpoint {} with consumerCommonName same as serviceProvider name, source {}", endpoint.getMessageBrokerUrl(), endpoint.getSource());
                                localEndpoints.add(endpointToLocalEndpoint(endpoint));
                            }
                            serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, localEndpoints);
                        } else {
                            //if (localSubscription.getConsumerCommonName().equals(subscription.getConsumerCommonName())) {
                                //throw new IllegalStateException("consumerCommonName is the same as Ixn name, local subscription user = subscription user");
                            //}
                            LocalEndpoint localEndpoint = new LocalEndpoint(serviceProvider.getName(), localMessageBrokerUrl);
                            logger.info("Adding local endpoint {} with consumerCommonName the same as Ixn name, queue {}", localEndpoint.getMessageBrokerUrl(), localEndpoint.getQueueName());
                            Set<LocalEndpoint> localEndpoints = new HashSet<>(Arrays.asList(localEndpoint));
                            serviceProvider.updateSubscriptionWithBrokerUrl(localSubscription, localEndpoints);
                        }
                    }
                }
            }
        }
    }

    public LocalEndpoint endpointToLocalEndpoint(Endpoint endpoint) {
        return new LocalEndpoint(endpoint.getSource(), endpoint.getMessageBrokerUrl());
    }
}
