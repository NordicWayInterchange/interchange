package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.serviceprovider.model.IxnServiceProvider;
import no.vegvesen.ixn.serviceprovider.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class OnboardRestController {

	private final ServiceProviderRepository repository;
	private Logger logger = LoggerFactory.getLogger(OnboardRestController.class);

	@Autowired
	public OnboardRestController(ServiceProviderRepository repository){
		this.repository = repository;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createServiceProvider")
	@ResponseBody
	public IxnServiceProvider updateSubscription(@RequestBody IxnServiceProvider serviceProvider){
		logger.debug("service provider name: " + serviceProvider.getName());
		IxnServiceProvider existing = repository.findByName(serviceProvider.getName());
		if (existing != null) {
			serviceProvider.setId(existing.getId());
		}
		return repository.save(serviceProvider);
	}

}
