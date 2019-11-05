package no.vegvesen.ixn.federation.server;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/test")
public abstract class NeighbourRestTestController {

	private NeighbourRepository neighbourRepository;
	private ServiceProviderRepository serviceProviderRepository;
	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	private int pollingCounter = 0;

	@Value("${Neighbour.node-provider.name}")
	private String myName;

	@Autowired
	public NeighbourRestTestController(NeighbourRepository NeighbourRepository, ServiceProviderRepository serviceProviderRepository){
		this.neighbourRepository = NeighbourRepository;
		this.serviceProviderRepository = serviceProviderRepository;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createMyRepresentation")
	@Secured("ROLE_USER")
	public ServiceProvider createMyRepresentation(@RequestBody ServiceProvider serviceProvider){
		serviceProviderRepository.save(serviceProvider);
		return serviceProvider;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/getStoredNeighbours")
	public List<Neighbour> getStoredNeighbours(){
		Iterable<Neighbour> storedNeighbours = neighbourRepository.findAll();

		List<Neighbour> Neighbours = new ArrayList<>();

		for(Neighbour i : storedNeighbours){
			Neighbours.add(i);
		}
		return Neighbours;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{ixnName}/updateSubscriptionStatus/{subscriptionId}")
	public Subscription updateSubscriptionStatus(@PathVariable String ixnName, @PathVariable Integer subscriptionId){

		Neighbour Neighbour = neighbourRepository.findByName(ixnName);

		if(Neighbour != null){
			try {
				Subscription updateSubscription = Neighbour.getSubscriptionById(subscriptionId);
				updateSubscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
				neighbourRepository.save(Neighbour);
				return updateSubscription;

			}catch(SubscriptionNotFoundException subscriptionNotFound){
				logger.error(subscriptionNotFound.getMessage());
				throw subscriptionNotFound;
			}
		} else{
			throw new InterchangeNotFoundException("The requested Neighbour does not exist. ");
		}
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = "/subscription", produces = MediaType.APPLICATION_JSON_VALUE)
	public SubscriptionRequestApi requestSubscriptions(@RequestBody SubscriptionRequestApi neighbourSubscriptionRequest) {

		int i=0;
		for(SubscriptionApi subscriptionApi : neighbourSubscriptionRequest.getSubscriptions()){
			i +=1;
			subscriptionApi.setPath("test/"+neighbourSubscriptionRequest.getName()+"/subscription/"+i);
		}

		throw new RuntimeException("Error error");
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.POST, value = "/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_USER")
	public CapabilityApi updateCapabilities(@RequestBody CapabilityApi neighbourCapabilities) {

		DataType volvoDataTypeOne = new DataType("datex2;1.0", "NO");
		DataType volvoDataTypeTwo = new DataType("datex2;1.0", "NO");

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setCapabilities(Stream.of(volvoDataTypeOne, volvoDataTypeTwo).collect(Collectors.toSet()));
		capabilityApi.setName("Remote host");

		throw new RuntimeException("error error");
	}

	// For testing polling
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(method = RequestMethod.GET, value = "{ixnName}/subscription/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public SubscriptionApi pollSubscriptionReturnsError(@PathVariable String ixnName, @PathVariable Integer subscriptionId) {

		pollingCounter +=1;
		logger.info("Poll nr: {}", pollingCounter);

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("where LIKE 'NO'");
		subscriptionApi.setPath(ixnName + "/subscription/"+subscriptionId);
		subscriptionApi.setStatus(SubscriptionStatus.REQUESTED);

		if(pollingCounter <= 2){
			logger.info(" - Returning subscription");
			return subscriptionApi;
		}else if(pollingCounter <= 4){
			logger.error(" - Throwing error!!!");
			throw new RuntimeException("Error error ");
		}else if(pollingCounter <= 6){
			logger.info(" - Returning subscription");
			return subscriptionApi;
		}else if(pollingCounter <= 9){
			logger.info(" - Throwing error!!!!");
			throw new RuntimeException("Error error again");
		}else{
			logger.info(" - Returning subscription.");
			return subscriptionApi;
		}

	}


}
