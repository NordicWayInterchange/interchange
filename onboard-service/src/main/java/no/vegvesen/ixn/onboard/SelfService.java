package no.vegvesen.ixn.onboard;


import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SelfService {
	private static Logger logger = LoggerFactory.getLogger(SelfService.class);
	private String nodeProviderName;
	ServiceProviderRepository repository;

	@Autowired
	public SelfService(@Value("${interchange.node-provider.name}") String nodeProviderName, ServiceProviderRepository repository) {
		this.nodeProviderName = nodeProviderName;
		this.repository = repository;
	}

	// Get the self representation from the database.
    //this could, of course, be a lot more efficient :-)
	public Self fetchSelf() {
		List<ServiceProvider> serviceProviders = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.CREATED);
		Self self = new Self(nodeProviderName);
		self.setLocalCapabilities(calculateSelfCapabilities(serviceProviders));
		self.setLocalSubscriptions(calculateSelfSubscriptions(serviceProviders));
		self.setLastUpdatedLocalSubscriptions(calculateLastUpdatedSubscriptions(serviceProviders));
		self.setLastUpdatedLocalCapabilities(calculateLastUpdatedCapabilties(serviceProviders));
        return self;

	}

	Set<DataType> calculateSelfCapabilities(Iterable<ServiceProvider> serviceProviders) {
		logger.info("Calculating Self capabilities...");
		Set<DataType> localCapabilities = new HashSet<>();

		for (ServiceProvider serviceProvider : serviceProviders) {
			logger.info("Service provider name: {}", serviceProvider.getName());
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities().getDataTypes();
			logger.info("Service Provider capabilities: {}", serviceProviderCapabilities.toString());
			localCapabilities.addAll(serviceProviderCapabilities);
		}
		logger.info("Calculated Self capabilities: {}", localCapabilities);
		return localCapabilities;
	}

	Set<DataType> calculateSelfSubscriptions(List<ServiceProvider> serviceProviders) {
		logger.info("Calculating Self subscriptions...");
		Set<DataType> localSubscriptions = new HashSet<>();

		for (ServiceProvider serviceProvider : serviceProviders) {
			logger.info("Service provider name: {}", serviceProvider.getName());
			Set<DataType> serviceProviderSubscriptions = serviceProvider
					.getSubscriptions()
					.stream()
					.filter(subscription -> LocalSubscriptionStatus.CREATED.equals(subscription.getStatus()))
					.map(LocalSubscription::getDataType)
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
		    for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
				LocalDateTime lastUpdated = subscription.getLastUpdated();
				if (lastUpdated != null) {
					if (result == null || lastUpdated.isAfter(result)) {
						result = lastUpdated;
					}
				}
			}

		}
	    return result;

	}


	LocalDateTime calculateLastUpdatedCapabilties(List<ServiceProvider> serviceProviders) {
	    LocalDateTime result = null;
		for (ServiceProvider serviceProvider : serviceProviders) {
		    Capabilities capabilities = serviceProvider.getCapabilities();
			LocalDateTime lastUpdated = capabilities.getLastUpdated();
			if (lastUpdated != null) {
				if (result == null || lastUpdated.isAfter(result)) {
					result = lastUpdated;
				}
			}
		}
	    return result;
	}

	public String getNodeProviderName() {
		return nodeProviderName;
	}
}
