package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//
// TODO: avoid hard coded paths for the API endpoints. Move them to application.properties
//
@RestController("/")
public class NeighbourRestController {

	private InterchangeRepository interchangeRepository;
	private ServiceProviderRepository serviceProviderRepository;
	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	@Value("${interchange.node-provider.name}")
	private String myName;



	@Autowired
	public NeighbourRestController(InterchangeRepository interchangeRepository, ServiceProviderRepository serviceProviderRepository) {
		this.interchangeRepository = interchangeRepository;
		this.serviceProviderRepository = serviceProviderRepository;
	}

	Set<Subscription> setStatusRequestedForAllSubscriptions(Set<Subscription> neighbourSubscriptionRequest){
		for (Subscription s : neighbourSubscriptionRequest){
			s.setSubscriptionStatus(Subscription.SubscriptionStatus.REQUESTED);
		}
		return neighbourSubscriptionRequest;
	}



	@ResponseStatus(HttpStatus.ACCEPTED)
	@RequestMapping(method = RequestMethod.POST, path = "/requestSubscription", produces = MediaType.APPLICATION_JSON_VALUE)
	public SubscriptionRequest requestSubscriptions(@RequestBody Interchange interchange){

		logger.info("Incoming interchange name: " + interchange.getName());

		// Returns a list of paths to poll for subscription status.
		Interchange updateInterchange = interchangeRepository.findByName(interchange.getName());
		logger.info("update interchange:" + updateInterchange);

		if(updateInterchange == null){
			// new neighbour - set capabilities status UNKNOWN to trigger capabilities exchange later
			logger.info("Unknown interchange requesting subscriptions. Updating capabilities status to UNKNOWN");
			updateInterchange = interchange;
			updateInterchange.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);

			logger.info("update interchange: " + updateInterchange.toString());

		} else{
			logger.info("*** interchange {} updated their subscription ***", updateInterchange.getName());
			logger.info("Saved interchange name: " + updateInterchange.getName());
			logger.info("Incoming interchange subscriptions: " + interchange.getSubscriptionRequest().toString());
		}

		logger.info("Subscription request: " + updateInterchange.getSubscriptionRequest().toString());

		Set<Subscription> requestedSubscriptions = setStatusRequestedForAllSubscriptions(interchange.getSubscriptionRequest().getSubscriptions());
		updateInterchange.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, requestedSubscriptions));

		logger.info("Updated interchange: " + updateInterchange.toString());

		// Save the interchange in the database to generate subscription ids.

		updateInterchange = interchangeRepository.save(updateInterchange);

		logger.info("*** After save");

		logger.info(updateInterchange.toString());
		logger.info("Saved neighbour in database");

		// Create a path for each subscription
		for (Subscription subscription : updateInterchange.getSubscriptionRequest().getSubscriptions()) {
			String path = updateInterchange.getName() + "/subscription/" + subscription.getId();
			logger.info("Path for subscription {}: {}", subscription.toString(), path);
			subscription.setPath(path);
		}

		interchangeRepository.save(updateInterchange);

		logger.info("Interchange updated with subscription: \n" + updateInterchange.toString());

		return updateInterchange.getSubscriptionRequest();
	}


	// TODO: update when protocol has been decided if we poll subscriptions or subscription requests.

	@RequestMapping(method = RequestMethod.GET, value = "{ixnName}/subscription/{subscriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
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

	@RequestMapping(method = RequestMethod.GET, value = "{ixnName}/subscriptionRequestStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	public SubscriptionRequest pollSubscriptionRequest(@PathVariable String ixnName){

		Interchange interchange = interchangeRepository.findByName(ixnName);

		if(interchange != null){
			return interchange.getSubscriptionRequest();
		}else{
			throw new InterchangeNotFoundException("The requested interchange does not exist.");
		}

	}

	// Method used to check for duplicate capabilities
	boolean setContainsDataType(DataType dataType, Set<DataType> capabilities){
		for(DataType d : capabilities){
			if(dataType.getHow().equals(d.getHow()) && dataType.getWhat().equals(d.getWhat()) && dataType.getWhere1().equals(d.getWhere1())){
				return true;
			}
		}
		return false;
	}

	protected Interchange getCapabilitiesOfDiscoveringNode(){

		logger.info("Getting capabilities of service providers for capabilities response.");

		Interchange discoveringInterchange = new Interchange();
		discoveringInterchange.setName(myName);
		Set<DataType> dataTypes = new HashSet<>();

		// Create Interchange capabilities from Service Provider capabilities.
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		logger.info("Found local service providers: \n");

		for(ServiceProvider serviceProvider : serviceProviders){
			logger.info(serviceProvider.toString());


			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities();
			for(DataType dataType : serviceProviderCapabilities){
				// Remove duplicate capabilities.
				if(!dataType.isContainedInSet(dataTypes)){
					dataTypes.add(dataType);
				}
			}
		}

		logger.info("Finished list of data types (capabilities: \n");

		for(DataType d : dataTypes){
			logger.info(d.toString());
		}

		Capabilities serviceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, dataTypes);
		discoveringInterchange.setCapabilities(serviceProviderCapabilities);
		logger.info("posting to neighbour: \n" + discoveringInterchange.toString());
		return discoveringInterchange;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(method = RequestMethod.POST, value = "/updateCapabilities", produces = MediaType.APPLICATION_JSON_VALUE)
	public Interchange updateCapabilities(@RequestBody Interchange interchange){

		logger.info("interchange name: " + interchange.getName());
		logger.info("Interchange capabilities: " + interchange.getCapabilities().toString());


		Interchange interchangeToUpdate = interchangeRepository.findByName(interchange.getName());

		if(interchangeToUpdate == null){
			logger.info("*** NEW INTERCHANGE **");
			interchangeToUpdate = interchange;
		}else{
			logger.info("---- UPDATING INTERCHANGE ----");
			interchangeToUpdate.setCapabilities(interchange.getCapabilities());
		}


		interchangeToUpdate.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		interchangeToUpdate.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.EMPTY);
		interchangeRepository.save(interchangeToUpdate);

		// Post response
		return getCapabilitiesOfDiscoveringNode();
	}

}
