package no.vegvesen.ixn.federation.dbhelper;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "db-helper.type", havingValue = "provider")
public class ProvidingInterchangeDbFiller implements DatabaseHelperInterface{

	private Logger logger = LoggerFactory.getLogger(ProvidingInterchangeDbFiller.class);
	private ServiceProviderRepository serviceProviderRepository;

	@Autowired
	public ProvidingInterchangeDbFiller(ServiceProviderRepository serviceProviderRepository){
		this.serviceProviderRepository = serviceProviderRepository;
	}


	// Capabilities: NO + SE
	// Subscriptions: DK
	@Override
	public void fillDatabase() {

		logger.info("DB helper type: producing");

		ServiceProvider teslaCloud = new ServiceProvider();
		teslaCloud.setName("Tesla Cloud");
		DataType teslaDataTypeOne = new DataType("datex2;1.0", "NO");
		DataType teslaDataTypeTwo = new DataType("datex2;1.0", "SE");
		Capabilities teslaCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(teslaDataTypeOne, teslaDataTypeTwo).collect(Collectors.toSet()));
		teslaCloud.setCapabilities(teslaCapabilities);


		Subscription teslaSubscription = new Subscription();
		teslaSubscription.setSelector("where LIKE 'DK'");
		teslaSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		SubscriptionRequest teslaSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED,Collections.singleton(teslaSubscription) );
		teslaCloud.setSubscriptionRequest(teslaSubscriptionRequest);
		serviceProviderRepository.save(teslaCloud);

		logger.info(teslaCloud.toString());
	}

}
