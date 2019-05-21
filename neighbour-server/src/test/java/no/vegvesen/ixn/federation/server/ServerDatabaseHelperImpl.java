package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.dbhelper.DatabaseHelperInterface;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ServerDatabaseHelperImpl implements DatabaseHelperInterface {

	private Logger logger = LoggerFactory.getLogger(ServerDatabaseHelperImpl.class);
	private ServiceProviderRepository serviceProviderRepository;

	@Autowired
	public ServerDatabaseHelperImpl(ServiceProviderRepository serviceProviderRepository){
		this.serviceProviderRepository = serviceProviderRepository;
	}

	@Override
	public void fillDatabase(){

		ServiceProvider volvoCloud = new ServiceProvider("Volvo Cloud");
		DataType volvoDataTypeOne = new DataType("datex2;1.0", "NO", "Works" );
		DataType volvoDataTypeTwo = new DataType("datex2;1.0", "NO", "Conditions");
		volvoCloud.setCapabilities(Stream.of(volvoDataTypeOne, volvoDataTypeTwo).collect(Collectors.toSet()));

		Subscription volvoSubscription = new Subscription();
		volvoSubscription.setSelector("where LIKE 'FI'");
		volvoSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		volvoCloud.setSubscriptions(Collections.singleton(volvoSubscription));
		serviceProviderRepository.save(volvoCloud);

		ServiceProvider scaniaCloud = new ServiceProvider("Scania Cloud");
		DataType scaniaDataTypeOne = new DataType("datex2;1.0", "SE", "Works");
		DataType scaniaDataTypeTwo = new DataType("datex2;1.0", "NO", "Works" );
		scaniaCloud.setCapabilities(Stream.of(scaniaDataTypeTwo, scaniaDataTypeOne).collect(Collectors.toSet()));

		Subscription scaniaSubscription = new Subscription();
		scaniaSubscription.setSelector("where LIKE 'DK'");
		scaniaSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		scaniaCloud.setSubscriptions(Collections.singleton(scaniaSubscription));
		serviceProviderRepository.save(scaniaCloud);

		logger.info(volvoCloud.toString());
		logger.info(scaniaCloud.toString());

	}
}
