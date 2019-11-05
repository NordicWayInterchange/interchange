package no.vegvesen.ixn.federation.dbhelper;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@ConditionalOnProperty(name = "db-helper.type", havingValue = "subscriber")
public class SubscribingInterchangeDbFiller implements DatabaseHelperInterface{

	private Logger logger = LoggerFactory.getLogger(SubscribingInterchangeDbFiller.class);
	private ServiceProviderRepository serviceProviderRepository;

	@Autowired
	public SubscribingInterchangeDbFiller(ServiceProviderRepository serviceProviderRepository){
		this.serviceProviderRepository = serviceProviderRepository;
	}

	// Capabilities: FI
	// Subscriptions: SE
	@Override
	public void fillDatabase() {

		logger.info("DB helper type: subscribing");

		ServiceProvider volvoCloud = new ServiceProvider();
		volvoCloud.setName("Volvo Cloud");
		DataType volvoDataTypeOne = new DataType("datex2;1.0", "FI");
		Capabilities volvoCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(volvoDataTypeOne));
		volvoCloud.setCapabilities(volvoCapabilities);

		Subscription volvoSubscriptions = new Subscription();
		volvoSubscriptions.setSelector("where LIKE 'SE'");
		volvoSubscriptions.setSubscriptionStatus(SubscriptionStatus.REQUESTED);
		SubscriptionRequest volvoSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(volvoSubscriptions));
		volvoCloud.setSubscriptionRequest(volvoSubscriptionRequest);
		serviceProviderRepository.save(volvoCloud);

		logger.info(volvoCloud.toString());
	}

}
