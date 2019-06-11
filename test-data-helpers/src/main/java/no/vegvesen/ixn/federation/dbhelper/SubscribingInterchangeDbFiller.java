package no.vegvesen.ixn.federation.dbhelper;

import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
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
@ConditionalOnProperty(name = "db-helper.type", havingValue = "subscriber")
public class SubscribingInterchangeDbFiller implements DatabaseHelperInterface{

	private Logger logger = LoggerFactory.getLogger(SubscribingInterchangeDbFiller.class);
	private ServiceProviderRepository serviceProviderRepository;

	@Autowired
	public SubscribingInterchangeDbFiller(ServiceProviderRepository serviceProviderRepository){
		this.serviceProviderRepository = serviceProviderRepository;
	}

	// Capabilities: FI, DK
	// Subscriptions: SE
	@Override
	public void fillDatabase() {

		logger.info("DB helper type: subscribing");

		ServiceProvider volvoCloud = new ServiceProvider();
		volvoCloud.setName("Volvo Cloud");
		DataType volvoDataTypeOne = new DataType("datex2;1.0", "FI", "Obstruction" );
		volvoCloud.setCapabilities(Collections.singleton(volvoDataTypeOne));

		Subscription volvoSubscriptions = new Subscription();
		volvoSubscriptions.setSelector("where LIKE 'SE'");
		volvoSubscriptions.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		volvoCloud.setSubscriptions(Collections.singleton(volvoSubscriptions));
		serviceProviderRepository.save(volvoCloud);

		logger.info(volvoCloud.toString());
	}

}
