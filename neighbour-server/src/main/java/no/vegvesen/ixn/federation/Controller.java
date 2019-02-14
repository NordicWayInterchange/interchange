package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.Model.Interchange;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller{

	@Autowired
	InterchangeRepository interchangeRepository;

	private Logger logger = LoggerFactory.getLogger(Controller.class);

	@PostMapping("/updateSubscription")
	public Interchange updateSubscription(@RequestBody Interchange interchange){

		Interchange mod = interchangeRepository.findByInterchangeId(interchange.getId());

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

		Interchange mod = interchangeRepository.findByInterchangeId(interchange.getId());

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


	@GetMapping("/getSubscription")
	public Interchange getSubscription(@RequestBody Interchange interchange){
		// TODO: return this node's subscriptions
		return interchange;
	}

	@GetMapping("/getCapabilities")
	public Interchange getCapabilities(@RequestBody Interchange interchange){
		// TODO: return this node's capabilities
		return interchange;
	}


	@GetMapping("/neighbours")
	public List<Interchange> getAllSubscriptions(){
		// Return a list of all the neighbours for debugging purposes.

		Iterable<Interchange> list = interchangeRepository.findAll();

		List<Interchange> ret = new ArrayList<>();

		for (Interchange i : list){
			ret.add(i);
		}

		return ret;
	}


}
