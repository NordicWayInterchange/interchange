package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotAcceptedException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Interchange.InterchangeStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
public class NeighbourRestController {

	private InterchangeRepository interchangeRepository;
	private ServiceProviderRepository serviceProviderRepository;
	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	@Value("${interchange.node-provider.name}")
	private String myName;

	@Autowired
	public NeighbourRestController(InterchangeRepository interchangeRepository, ServiceProviderRepository serviceProviderRepository){
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
	}

	private Set<Subscription> setStatusRequestedForAllSubscriptions(Set<Subscription> neighbourSubscriptionRequest){
		for (Subscription s : neighbourSubscriptionRequest){
			s.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		}
		return neighbourSubscriptionRequest;
	}


	// TODO: Endre denne returtypen til en liste/set av subscriptions,
	// TODO: hvor hver subscription har en path og en status.
	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, value = "/requestSubscription")
	public List<String> requestSubscriptions(@RequestBody Interchange interchange){

		// Returns a list of paths to poll for subscription status.
		Interchange updateInterchange = interchangeRepository.findByName(interchange.getName());
		logger.info("interchange name: " + interchange.getName());
		logger.info("Interchange subscriptions: " + interchange.getSubscriptions().toString());

		List<String> paths = new ArrayList<>();

		if(updateInterchange == null){
			logger.error("Unknown interchange requesting subscriptions. REJECTED.");
			throw new InterchangeNotFoundException("Capabilities must be exchanged before it is possible to post a subscription request.");
		}else if(updateInterchange.getInterchangeStatus() != InterchangeStatus.KNOWN || updateInterchange.getInterchangeStatus() != InterchangeStatus.FEDERATED){
			logger.error("Interchange with status other than KNOWN or FEDERATED tried to post a subscription request. REJECTED.");
			throw new SubscriptionNotAcceptedException("Only KNOWN or FEDERATED interchanges may post subscription requests.");
		}
		else{
			logger.info("*** {} interchange {} updated their subscription ***", interchange.getInterchangeStatus().toString(), interchange.getName());
			LocalDateTime now = LocalDateTime.now();
			Set<Subscription> requestedSubscriptions = setStatusRequestedForAllSubscriptions(interchange.getSubscriptions());
			updateInterchange.setSubscriptions(requestedSubscriptions);
			updateInterchange.setLastSeen(now);
			updateInterchange = interchangeRepository.save(updateInterchange);
		}

		// Create paths to return.
		for (Subscription subscription : updateInterchange.getSubscriptions()) {
			String path = interchange.getName() + "/subscription/" + subscription.getId();
			paths.add(path);
		}
		return paths;
	}


	@RequestMapping(method = RequestMethod.GET, value = "{ixnName}/subscription/{subscriptionId}")
	public Subscription pollSubscription(@PathVariable String ixnName, @PathVariable Integer subscriptionId){

		Interchange interchange = interchangeRepository.findByName(ixnName);

		if(interchange != null){
			try {
				return interchange.getSubscriptionById(subscriptionId);
			}catch(SubscriptionNotFoundException subscriptionNotFound){
				logger.error(subscriptionNotFound.getMessage());
				throw subscriptionNotFound;
			}
		} else{
			throw new InterchangeNotFoundException("The requested interchange does not exist. ");
		}
	}

	// Method used to check for duplicate capabilities
	private boolean setContainsDataType(DataType dataType, Set<DataType> capabilities){
		for(DataType d : capabilities){
			if(dataType.getHow().equals(d.getHow()) && dataType.getWhat().equals(d.getWhat()) && dataType.getWhere1().equals(d.getWhere1())){
				return true;
			}
		}
		return false;
	}

	private Interchange getCapabilitiesOfDiscoveringNode(){

		Interchange discoveringInterchange = new Interchange();
		discoveringInterchange.setName(myName);
		Set<DataType> interchangeCapabilities = new HashSet<>();

		// Create Interchange capabilities from Service Provider capabilities.
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		for(ServiceProvider serviceProvider : serviceProviders){
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities();
			for(DataType dataType : serviceProviderCapabilities){
				// Remove duplicate capabilities.
				if(!setContainsDataType(dataType, interchangeCapabilities)){
					interchangeCapabilities.add(dataType);
				}
			}
		}
		discoveringInterchange.setCapabilities(interchangeCapabilities);
		return discoveringInterchange;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(method = RequestMethod.POST, value = "/updateCapabilities")
	public Interchange updateCapabilities(@RequestBody Interchange interchange){

		Interchange interchangeToUpdate = interchangeRepository.findByName(interchange.getName());
		logger.info("interchange name: " + interchange.getName());
		logger.info("Interchange capabilities: " + interchange.getCapabilities().toString());

		LocalDateTime now = LocalDateTime.now();

		if(interchangeToUpdate == null){
			logger.info("*** NEW INTERCHANGE **");
			interchange.setLastSeen(now);
			// The interchange contacted us directly. Give status 'KNOWN'
			interchange.setInterchangeStatus(InterchangeStatus.KNOWN);
			interchangeRepository.save(interchange);
		}else{
			logger.info("---- UPDATING INTERCHANGE ----");
			interchangeToUpdate.setLastSeen(now);
			interchangeToUpdate.setCapabilities(interchange.getCapabilities());
			interchangeRepository.save(interchangeToUpdate);
		}

		// Post response
		return getCapabilitiesOfDiscoveringNode();
	}

}
