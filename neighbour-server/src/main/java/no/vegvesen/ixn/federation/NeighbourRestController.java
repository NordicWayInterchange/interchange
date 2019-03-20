package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.exceptions.InterchangeNotFoundException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRejectedException;
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

	@RequestMapping(method = RequestMethod.POST, value = "/requestSubscription")
	public List<String> requestSubscriptions(@RequestBody Interchange interchange){

		// Returns a list of paths to poll for subscription status.
		Interchange updateInterchange = interchangeRepository.findByName(interchange.getName());
		logger.info("interchange name: " + interchange.getName());
		logger.info("Interchange subscriptions: " + interchange.getSubscriptions().toString());

		List<String> paths = new ArrayList<>();

		if(updateInterchange == null){
			logger.info("*** Unknown interchange requesting subscriptions. Rejecting... ***");
			throw new SubscriptionRejectedException("Capabilities must be exchanged before it is possible to post a subscription request.");
		}else{
			// Interchange exists: update it.
			logger.info("*** Known interchange updated their subscription ***");
			LocalDateTime now = LocalDateTime.now();
			updateInterchange.setSubscriptions(interchange.getSubscriptions());
			updateInterchange.setLastSeen(now);
			updateInterchange = interchangeRepository.save(updateInterchange);
		}
		// Create paths to return.
		for (Subscription subscription : updateInterchange.getSubscriptions()) {
			// Path is interchange name and subscription id.
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
			}catch(Exception e){
				logger.info(e.getMessage());
				throw new InterchangeNotFoundException("The interchange has no subscription with this id. " + e.getMessage());
			}
		} else{
			throw new InterchangeNotFoundException("The requested interchange does not exist. ");
		}
	}

	// Method used to check for duplicate capabilities
	private boolean setContainsObject(DataType dataType, Set<DataType> capabilities){

		for(DataType d : capabilities){
			if(dataType.getHow().equals(d.getHow()) && dataType.getWhat().equals(d.getWhat()) && dataType.getWhere1().equals(d.getWhere1())){
				return true;
			}
		}
		return false;
	}

	private Interchange getCapabilitiesOfCurrentNode(){
		// Create POST response: My capabilities.
		Interchange myRepresentation = new Interchange();
		myRepresentation.setName(myName);
		Set<DataType> interchangeCapabilities = new HashSet<>();

		// Create Interchange capabilities from Service Provider capabilities.
		Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

		for(ServiceProvider serviceProvider : serviceProviders){
			Set<DataType> serviceProviderCapabilities = serviceProvider.getCapabilities();
			for(DataType dataType : serviceProviderCapabilities){
				// Remove duplicate capabilities.
				if(!setContainsObject(dataType, interchangeCapabilities)){
					interchangeCapabilities.add(dataType);
				}
			}
		}

		myRepresentation.setCapabilities(interchangeCapabilities);
		return myRepresentation;
	}

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
		return getCapabilitiesOfCurrentNode();
	}

	// TODO: Remove
	@RequestMapping(method = RequestMethod.POST, value = "/createMyRepresentation")
	public ServiceProvider createMyRepresentation(@RequestBody ServiceProvider serviceProvider){

		serviceProviderRepository.save(serviceProvider);
		return serviceProvider;
	}

	// TODO: Remove
	@RequestMapping(method = RequestMethod.GET, value = "/getStoredInterchanges")
	public List<Interchange> getStoredInterchanges(){
		Iterable<Interchange> storedInterchanges = interchangeRepository.findAll();

		List<Interchange> interchanges = new ArrayList<>();

		for(Interchange i : storedInterchanges){
			interchanges.add(i);
		}

		return interchanges;
	}

}
