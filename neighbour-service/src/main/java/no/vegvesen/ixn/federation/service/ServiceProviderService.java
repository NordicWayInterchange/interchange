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

    public List<ServiceProvider> findAllServiceProviders() {
        return serviceProviderRepository.findAll();
    }

    //TODO don't need Self here. Getting the messageChannelUrl should be somwhere else?
    public void updateLocalSubscriptions(String messageChannelHost, String messageChannelPort) {
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        List<Neighbour> neighbours = neighbourRepository.findAll();
        for(ServiceProvider serviceProvider : serviceProviders) {
            updateServiceProviderSubscriptionsWithHostAndPort(neighbours, serviceProvider, messageChannelHost, messageChannelPort);
            serviceProviderRepository.save(serviceProvider);
        }
    }
    //TODO: make test for this method
    public void updateServiceProviderSubscriptionsWithHostAndPort(List<Neighbour> neighbours, ServiceProvider serviceProvider, String messageChannelHost, String messageChannelPort) {
        for(Neighbour neighbour : neighbours) {
            for(LocalSubscription localSubscription : serviceProvider.getSubscriptions()){
                for(Subscription subscription : neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions()){
                    if (localSubscription.getSelector().equals(subscription.getSelector())) {
                        if (localSubscription.getConsumerCommonName().equals(serviceProvider.getName())) {
                            //TODO What about changes to endpoints? We also write ALL service provider Endpoints every time!
                            Set<Endpoint> endpoints = subscription.getEndpoints();
                            Set<LocalEndpoint> localEndpoints = new HashSet<>();
                            for (Endpoint endpoint : endpoints) {
                                logger.info("Adding local endpoint with host {} and consumerCommonName same as serviceProvider name, source {}", endpoint.getHost(), endpoint.getSource());
                                localEndpoints.add(endpointToLocalEndpoint(endpoint));
                            }
                            serviceProvider.updateSubscriptionWithHostAndPort(localSubscription, localEndpoints);
                        } else {
                            LocalEndpoint localEndpoint = new LocalEndpoint(localSubscription.getQueueName(), messageChannelHost, Integer.parseInt(messageChannelPort));
                            logger.info("Adding local endpoint with host {} and port {}, consumerCommonName the same as Ixn name, queue {}", localEndpoint.getHost(), localEndpoint.getPort(), localEndpoint.getSource());
                            Set<LocalEndpoint> localEndpoints = new HashSet<>(Arrays.asList(localEndpoint));
                            serviceProvider.updateSubscriptionWithHostAndPort(localSubscription, localEndpoints);
                        }
                    }
                }
            }
        }
    }

    public LocalEndpoint endpointToLocalEndpoint(Endpoint endpoint) {
        return new LocalEndpoint(endpoint.getSource(), endpoint.getHost(), endpoint.getPort());
    }
}
