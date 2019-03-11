package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController
public class NeighbourRestController {

	private InterchangeRepository interchangeRepository;
	private Logger logger = LoggerFactory.getLogger(NeighbourRestController.class);

	@Autowired
	public NeighbourRestController(InterchangeRepository interchangeRepository){
		this.interchangeRepository = interchangeRepository;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createSubscription")
	public List<String> createSubscriptions(@RequestBody Interchange interchange){

		// Return a list of paths to poll
		// Get the interchange we are creating subscriptions for.
		Interchange updateInterchange = interchangeRepository.findByName(interchange.getName());

		logger.info("interchange name: " + interchange.getName());
		logger.info("Interchange subscriptions: " + interchange.getSubscriptions().toString());

		List<String> paths = new ArrayList<>();

		if(updateInterchange == null){
			// Interchange does not exist. Create it.
			logger.info("*** NEW INTERCHANGE **");
			updateInterchange = interchangeRepository.save(interchange);
		}else{
			// Interchange exists: update it.
			logger.info("---- UPDATING INTERCHANGE ----");
			updateInterchange.setSubscriptions(interchange.getSubscriptions());
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
			// found the interchange, get the subscription
			try {
				Subscription subscription = interchange.getSubscriptionById(subscriptionId);
				return subscription;
			}catch(Exception e){
				logger.info(e.getMessage());
			}
		}

		return null;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateCapabilities")
	public Interchange updateCapabilities(@RequestBody Interchange interchange){

		Interchange mod = interchangeRepository.findByName(interchange.getName());
		logger.info("interchange name: " + interchange.getName());
		logger.info("Interchange capabilities: " + interchange.getCapabilities().toString());

		if(mod == null){
			interchangeRepository.save(interchange);
			logger.info("*** NEW INTERCHANGE **");
		}else{
			logger.info("---- UPDATING INTERCHANGE ----");
			mod.setCapabilities(interchange.getCapabilities());
			interchangeRepository.save(mod);
		}

		return interchange;
	}

	@RequestMapping(method = RequestMethod.GET, value="/{ixnName}/subscriptions")
	public Set<Subscription> getSubscriptions(@PathVariable String ixnName){
		// Get the subscriptions for a given node.
		Interchange mod = interchangeRepository.findByName(ixnName);

		if(mod != null){
			return mod.getSubscriptions();
		}

		return Collections.emptySet();
	}

	@RequestMapping(method = RequestMethod.GET, value="/{ixnId}/capabilities")
	public Set<DataType> getCapabilities(@PathVariable String ixnId){
		// Get the capabilities for a given node.
		Interchange mod = interchangeRepository.findByName(ixnId);

		if(mod != null){
			return mod.getCapabilities();
		}

		return Collections.emptySet();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/checkForChangesSince/{timestamp}")
	public List<Interchange> checkForChanges(@PathVariable Timestamp timestamp){
		Instant now = Instant.now();
		Timestamp nowTimestamp = Timestamp.from(now);

		logger.info("Created timestamp from now: " + nowTimestamp.toString());
		logger.info("Create timestamp from incoming json string: " + timestamp.toString());
		try {

			return interchangeRepository.findInterchangeOlderThan(timestamp, nowTimestamp);
		}catch(Exception e){
			logger.info(e.getClass().getName());
			logger.info("Timestamp: " + timestamp + ", found no objects older than this time. ");
		}

		return Collections.emptyList();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/printNeighbours")
	public List<Interchange> getAllSubscriptions(){
		// Return a list of all the registered interchanges(neighbours) for debugging purposes.

		Iterable<Interchange> interchanges = interchangeRepository.findAll();
		List<Interchange> returnList = new ArrayList<>();

		for (Interchange i : interchanges){
			returnList.add(i);
		}

		return returnList;
	}


}
