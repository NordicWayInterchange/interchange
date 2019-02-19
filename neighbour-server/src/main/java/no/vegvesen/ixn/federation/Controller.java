package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.Model.Capability;
import no.vegvesen.ixn.federation.model.Model.Interchange;
import no.vegvesen.ixn.federation.model.Model.Subscription;
import no.vegvesen.ixn.federation.model.Repository.InterchangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class Controller {

	@Autowired
	InterchangeRepository interchangeRepository;

	Logger logger = LoggerFactory.getLogger(Controller.class);

	@PostMapping("/updateSubscription")
	public Interchange updateSubscription(@RequestBody Interchange interchange){

		Interchange mod = interchangeRepository.findByInterchangeId(interchange.getName());

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

	@PostMapping("/updateCapabilities")
	public Interchange updateCapabilities(@RequestBody Interchange interchange){

		Interchange mod = interchangeRepository.findByInterchangeId(interchange.getName());

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


	@RequestMapping(path="/{ixnId}/subscriptions")
	public Set<Subscription> getSubscription(@PathVariable String ixnId){
		// TODO: return all the subscriptions

		Interchange mod = interchangeRepository.findByInterchangeId(ixnId);

		if(mod != null){
			return mod.getSubscriptions();
		}

		return Collections.emptySet();
	}

	@RequestMapping(path="/{ixnId}/capabilities")
	public Set<Capability> getCapabilities(@PathVariable String ixnId){
		// TODO: return this node's capabilities

		Interchange mod = interchangeRepository.findByInterchangeId(ixnId);

		if(mod != null){
			return mod.getCapabilities();
		}

		return Collections.emptySet();
	}

	@RequestMapping(path="/{ixnId}")
	public Interchange getInterchange(@PathVariable String ixnId){
		// TODO: return this node's capabilities

		Interchange mod = interchangeRepository.findByInterchangeId(ixnId);

		if(mod != null){
			return mod;
		}

		return null;
	}


	@RequestMapping(path = "/checkForChanges/{timestamp}")
	public List<Interchange> checkForChanges(@PathVariable String timestamp){

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
		LocalDateTime formattedTime = LocalDateTime.parse(timestamp, formatter);

		String queryAllOlderThan = formattedTime.format(formatter);

		logger.info("Querying for Interchanges last updated before: " + queryAllOlderThan);

		try {

			return interchangeRepository.findOlderThan(timestamp);
		}catch(Exception e){
			logger.info(e.getClass().getName());
			logger.info("Timestamp: " + timestamp + ", found no objects older than this time. ");
		}

		return Arrays.asList();
	}


	@GetMapping("/printNeighbours")
	public List<Interchange> getAllSubscriptions(){
		// Return a list of all the neighbours for debugging purposes.

		Iterable<Interchange> list = interchangeRepository.findAll();

		List<Interchange> ret = new ArrayList<>();

		for (Interchange i : list){
			ret.add(i);
		}

		if(ret.size() == 0){
			return Arrays.asList();
		}else{
			return ret;
		}

	}


}
