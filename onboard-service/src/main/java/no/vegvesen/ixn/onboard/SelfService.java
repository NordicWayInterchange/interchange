package no.vegvesen.ixn.onboard;


import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ConfigurationPropertiesScan
public class SelfService {
	private static Logger logger = LoggerFactory.getLogger(SelfService.class);
	ServiceProviderRepository repository;
	InterchangeNodeProperties interchangeNodeProperties;


	@Autowired
	public SelfService(InterchangeNodeProperties interchangeNodeProperties, ServiceProviderRepository repository) {
		this.interchangeNodeProperties = interchangeNodeProperties;
		this.repository = repository;
	}

	// Get the self representation from the database.
    //this could, of course, be a lot more efficient :-)
	public Self fetchSelf() {
		List<ServiceProvider> serviceProviders = repository.findAll();
		Self self = new Self(interchangeNodeProperties.getName());
		self.setLocalCapabilities(CapabilityCalculator.calculateSelfCapabilities(serviceProviders));
		self.setLocalSubscriptions(calculateSelfSubscriptions(serviceProviders));
		self.setLastUpdatedLocalSubscriptions(calculateLastUpdatedSubscriptions(serviceProviders));
		self.setLastUpdatedLocalCapabilities(calculateLastUpdatedCapabilities(serviceProviders));
		self.setMessageChannelPort(interchangeNodeProperties.getMessageChannelPort());
        return self;

	}

	Set<LocalSubscription> calculateSelfSubscriptions(List<ServiceProvider> serviceProviders) {
		logger.info("Calculating Self subscriptions...");
		Set<LocalSubscription> localSubscriptions = new HashSet<>();

		for (ServiceProvider serviceProvider : serviceProviders) {
			logger.info("Service provider name: {}", serviceProvider.getName());
			Set<LocalSubscription> serviceProviderSubscriptions = serviceProvider
					.getSubscriptions()
					.stream()
					.filter(subscription -> LocalSubscriptionStatus.CREATED.equals(subscription.getStatus()))
					.collect(Collectors.toSet());
			logger.info("Service Provider Subscriptions: {}", serviceProviderSubscriptions.toString());
			localSubscriptions.addAll(serviceProviderSubscriptions);
		}
		logger.info("Calculated Self subscriptions: {}", localSubscriptions.toString());
		return localSubscriptions;
	}

	LocalDateTime calculateLastUpdatedSubscriptions(List<ServiceProvider> serviceProviders) {
	    LocalDateTime result = null;
		for (ServiceProvider serviceProvider : serviceProviders) {
			Optional<LocalDateTime> lastUpdated = serviceProvider.getSubscriptionUpdated();
			if (lastUpdated.isPresent()) {
				if (result == null || lastUpdated.get().isAfter(result)) {
					result = lastUpdated.get();
				}
			}
		}
	    return result;
	}

	LocalDateTime calculateLastUpdatedCapabilities(List<ServiceProvider> serviceProviders) {
	    LocalDateTime result = null;
		for (ServiceProvider serviceProvider : serviceProviders) {
		    Capabilities capabilities = serviceProvider.getCapabilities();
			Optional<LocalDateTime> lastUpdated = capabilities.getLastUpdated();
			if (lastUpdated.isPresent()) {
				if (result == null || lastUpdated.get().isAfter(result)) {
					result = lastUpdated.get();
				}
			}
		}
	    return result;
	}

	//TODO: decide if centralize to the Properties class, or the service
	public String getNodeProviderName() {
		return interchangeNodeProperties.getName();
	}

	public String getMessageChannelUrl() {
		return Self.getMessageChannelUrl(interchangeNodeProperties.getName(),interchangeNodeProperties.getMessageChannelPort());
	}
}
