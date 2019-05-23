package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
public abstract class NeighbourRestTestController {

	private InterchangeRepository interchangeRepository;
	private ServiceProviderRepository serviceProviderRepository;
	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	private int pollingCounter = 0;

	@Value("${interchange.node-provider.name}")
	private String myName;

	@Autowired
	public NeighbourRestTestController(InterchangeRepository interchangeRepository, ServiceProviderRepository serviceProviderRepository){
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createMyRepresentation")
	@Secured("ROLE_USER")
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

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = "/subscription", produces = MediaType.APPLICATION_JSON_VALUE)
	public SubscriptionRequestApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {

		int i=0;
		for(Subscription subscription : neighbourSubscriptionRequest.getSubscriptions()){
			i +=1;
			subscription.setPath("test/"+neighbourSubscriptionRequest.getName()+"/subscription/"+i);
			subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		}

		return neighbourSubscriptionRequest;
	}

	// For testing polling
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = "{ixnName}/subscription/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Subscription pollSubscriptionReturnsError(@PathVariable String ixnName, @PathVariable Integer subscriptionId) {

		pollingCounter +=1;
		logger.info("Poll nr: {}", pollingCounter);


		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		subscription.setPath(ixnName + "/subscription/"+subscriptionId);
		subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);


		if(pollingCounter <= 2){
			logger.info(" - Returning subscription");
			return subscription;
		}else if( pollingCounter >= 3 && pollingCounter <=4){
			logger.error(" - Throwing error!!!");
			throw new RuntimeException("Error error ");
		}else if(pollingCounter >= 5 && pollingCounter <= 6){
			logger.info(" - Returning subscription");
			return subscription;
		}else if(pollingCounter >= 7 && pollingCounter <= 9){
			logger.info(" - Throwing error!!!!");
			throw new RuntimeException("Error error again");
		}else{
			logger.info(" - Returning subscription.");
			return subscription;
		}


	}


}
