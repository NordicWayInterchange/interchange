package no.vegvesen.ixn.federation.discoverer;

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
public class ClientDatabaseHelperImpl implements DatabaseHelperInterface {


	private Logger logger = LoggerFactory.getLogger(ClientDatabaseHelperImpl.class);
	private ServiceProviderRepository serviceProviderRepository;

	@Autowired
	public ClientDatabaseHelperImpl(ServiceProviderRepository serviceProviderRepository){
		this.serviceProviderRepository = serviceProviderRepository;
	}


	@Override
	public void fillDatabase() {

		ServiceProvider teslaCloud = new ServiceProvider();
		teslaCloud.setName("Tesla Cloud");
		DataType teslaDataTypeOne = new DataType("datex2;1.0", "FI", "Obstruction" );
		DataType teslaDataTypeTwo = new DataType("datex2;1.0", "FI", "Conditions");
		teslaCloud.setCapabilities(Stream.of(teslaDataTypeOne, teslaDataTypeTwo).collect(Collectors.toSet()));

		Subscription teslaSubscription = new Subscription();
		teslaSubscription.setSelector("where1 LIKE 'NO'");
		teslaSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		teslaCloud.setSubscriptions(Collections.singleton(teslaSubscription));
		serviceProviderRepository.save(teslaCloud);

		ServiceProvider BMWCloud = new ServiceProvider();
		BMWCloud.setName("BMW Cloud");
		DataType bmwDataTypeOne = new DataType("datex2;1.0", "DK", "Conditions");
		DataType bmwDataTypeTwo = new DataType("datex2;1.0", "FI", "Conditions");
		BMWCloud.setCapabilities(Stream.of(bmwDataTypeTwo, bmwDataTypeOne).collect(Collectors.toSet()));

		Subscription BMWSubscription = new Subscription();
		BMWSubscription.setSelector("where1 LIKE 'SE'");
		BMWSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		BMWCloud.setSubscriptions(Collections.singleton(BMWSubscription));
		serviceProviderRepository.save(BMWCloud);

		logger.info(teslaCloud.toString());
		logger.info(BMWCloud.toString());

	}

}
