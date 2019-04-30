package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
public class NeighbourRestTestController {

	private InterchangeRepository interchangeRepository;
	private ServiceProviderRepository serviceProviderRepository;
	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	@Value("${interchange.node-provider.name}")
	private String myName;

	@Autowired
	public NeighbourRestTestController(InterchangeRepository interchangeRepository, ServiceProviderRepository serviceProviderRepository){
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createMyRepresentation")
	public ServiceProvider createMyRepresentation(@RequestBody ServiceProvider serviceProvider){
		serviceProviderRepository.save(serviceProvider);
		return serviceProvider;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getStoredInterchanges")
	public List<Interchange> getStoredInterchanges(){
		Iterable<Interchange> storedInterchanges = interchangeRepository.findAll();

		List<Interchange> interchanges = new ArrayList<>();

		for(Interchange i : storedInterchanges){
			interchanges.add(i);
		}
		return interchanges;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{ixnName}/updateSubscriptionStatus/{subscriptionId}")
	public Subscription updateSubscriptionStatus(@PathVariable String ixnName, @PathVariable Integer subscriptionId){

		Interchange interchange = interchangeRepository.findByName(ixnName);

		if(interchange != null){
			try {
				Subscription updateSubscription = interchange.getSubscriptionById(subscriptionId);
				updateSubscription.setSubscriptionStatus(Subscription.SubscriptionStatus.CREATED);
				interchangeRepository.save(interchange);
				return updateSubscription;

			}catch(SubscriptionNotFoundException subscriptionNotFound){
				logger.error(subscriptionNotFound.getMessage());
				throw subscriptionNotFound;
			}
		} else{
			throw new InterchangeNotFoundException("The requested interchange does not exist. ");
		}
	}
}
