package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.IxnServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class OnboardRestController {

	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);

	@Autowired
	public OnboardRestController(){
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createServiceProvider")
	public IxnServiceProvider updateSubscription(@RequestBody IxnServiceProvider serviceProvider){
		logger.info("interchange name: " + serviceProvider.getName());
		serviceProvider.setId(UUID.randomUUID().toString());
		return serviceProvider;
	}

}
