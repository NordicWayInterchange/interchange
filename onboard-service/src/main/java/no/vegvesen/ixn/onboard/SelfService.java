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

	// Get the self representation from the database. If it doesn't exist, create it.
	public Self fetchSelf() {
		List<ServiceProvider> serviceProviders = repository.findBySubscriptions_StatusIn(LocalSubscriptionStatus.CREATED);
		Self self = new Self(nodeProviderName);
		self.setLocalCapabilities(calculateSelfCapabilities(serviceProviders));
		self.setLocalSubscriptions(calculateSelfSubscriptions(serviceProviders));
		//TODO also have to remember LastUpdated....
        return self;

	}

/*
	public void updateSelfCapabilities(Set<DataType> previousCapabilities, List<ServiceProvider> serviceProviders) {
		// Recalculate Self capabilities now that we updated a Service Provider.
		Self self = fetchSelf();
		Set<DataType> updatedCapabilities = calculateSelfCapabilities(serviceProviders);

		// If old version and updated version of the capabilities are not equal, then we update the timestamp
		logger.info("Previous capabilities: {}", previousCapabilities);
		logger.info("Updated capabilities:  {}", updatedCapabilities);

		if (!previousCapabilities.equals(updatedCapabilities)) {
			logger.info("Capabilities have changed. Updating representation of self.");
			self.setLastUpdatedLocalCapabilities(LocalDateTime.now());
			self.setLocalCapabilities(updatedCapabilities);
			selfRepository.save(self);
			logger.info("Updated Self: {}", self.toString());
		} else {
			logger.info("Capabilities have not changed. Keeping the current representation of self.");
		}
	}
*/
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

	public void updateSelfSubscriptions(Set<DataType> previousSubscriptions, List<ServiceProvider> serviceProviders) {
		// Get the current Self subscriptions. Recalculate the Self subscriptions now that a Service Provider has been updated.
		Self self = fetchSelf();
		Set<DataType> updatedSubscriptions = calculateSelfSubscriptions(serviceProviders);

		if (!previousSubscriptions.equals(updatedSubscriptions)) {
			logger.debug("Subscriptions have changed from {} to {}. Updating representation of self.",
					previousSubscriptions.toString(),
					updatedSubscriptions.toString());
			self.setLocalSubscriptions(updatedSubscriptions);
			self.setLastUpdatedLocalSubscriptions(LocalDateTime.now());
			self = save(self);
			logger.info("Updated Self: {}", self.toString());
		} else {
			logger.debug("Subscriptions have not changed from {} to {}. No self update.",
					previousSubscriptions.toString(),
					updatedSubscriptions.toString());
		}
	}

	Set<DataType> calculateSelfSubscriptions(List<ServiceProvider> serviceProviders) {
		logger.info("Calculating Self subscriptions...");
		Set<DataType> localSubscriptions = new HashSet<>();

		for (ServiceProvider serviceProvider : serviceProviders) {
			logger.info("Service provider name: {}", serviceProvider.getName());
			//Set<DataType> serviceProviderSubscriptions = serviceProvider.getOrCreateLocalSubscriptionRequest().getSubscriptions();
			Set<DataType> serviceProviderSubscriptions = serviceProvider
					.getSubscriptions()
					.stream()
					.map(LocalSubscription::getDataType)
					.collect(Collectors.toSet());
			logger.info("Service Provider Subscriptions: {}", serviceProviderSubscriptions.toString());
			localSubscriptions.addAll(serviceProviderSubscriptions);
		}
		logger.info("Calculated Self subscriptions: {}", localSubscriptions.toString());
		return localSubscriptions;
	}



	//TODO go through uses of this, and remove the calls.
	public Self save(Self self) {
		/*
		return selfRepository.save(self);
		*/
		throw new IllegalStateException("You really shouldn't save!");
	}
}
