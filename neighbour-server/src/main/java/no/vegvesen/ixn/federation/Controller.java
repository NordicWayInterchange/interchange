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
public class Controller {

	private InterchangeRepository interchangeRepository;
	private Logger logger = LoggerFactory.getLogger(Controller.class);

	@Autowired
	public Controller(InterchangeRepository interchangeRepository){
		this.interchangeRepository = interchangeRepository;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/updateSubscription")
	public Interchange updateSubscription(@RequestBody Interchange interchange){

		Interchange mod = interchangeRepository.findByName(interchange.getName());

		logger.info("interchange name: " + interchange.getName());
		logger.info("Interchange subscriptions: " + interchange.getSubscriptions().toString());

		if(mod == null){
			interchangeRepository.save(interchange);
			logger.info("*** NEW INTERCHANGE **");
		}else{
			logger.info("---- UPDATING INTERCHANGE ----");
			mod.setSubscriptions(interchange.getSubscriptions());
			interchangeRepository.save(mod);
		}

		return interchange;
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
	public Set<Subscription> getSubscription(@PathVariable String ixnName){
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

	@RequestMapping(method = RequestMethod.POST, value="/{ixnId}")
	public Interchange getInterchange(@PathVariable String ixnId){
		// Return the given interchange object.
		Interchange mod = interchangeRepository.findByName(ixnId);

		if(mod != null){
			return mod;
		}

		return null;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/checkForChangesSince/{timestamp}")
	public List<Interchange> checkForChanges(@PathVariable Timestamp timestamp){
		Instant now = Instant.now();
		Timestamp nowTimestamp = Timestamp.from(now);

		logger.info("Created timestamp from now: " + nowTimestamp.toString());
		logger.info("Create timestamp from incoming json string: " + timestamp.toString());
		try {

			return interchangeRepository.findOlderThan(timestamp, nowTimestamp);
		}catch(Exception e){
			logger.info(e.getClass().getName());
			logger.info("Timestamp: " + timestamp + ", found no objects older than this time. ");
		}

		return Collections.emptyList();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/printNeighbours")
	public List<Interchange> getAllSubscriptions(){
		// Return a list of all the registered interchanges(neighbours) for debugging purposes.

		Iterable<Interchange> list = interchangeRepository.findAll();

		List<Interchange> ret = new ArrayList<>();

		for (Interchange i : list){
			ret.add(i);
		}

		if(ret.size() == 0){
			return Collections.emptyList();
		}else{
			return ret;
		}
	}


}
