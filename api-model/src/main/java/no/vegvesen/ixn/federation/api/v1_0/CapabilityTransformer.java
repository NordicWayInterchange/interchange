package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import org.springframework.stereotype.Component;

@Component
public class CapabilityTransformer {

	public CapabilityApi neighbourToCapabilityApi(Neighbour neighbour){

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(neighbour.getName());
		capabilityApi.setCapabilities(neighbour.getCapabilities().getDataTypes());

		return capabilityApi;
	}

	public Neighbour capabilityApiToNeighbour(CapabilityApi capabilityApi){

		Neighbour neighbour = new Neighbour();
		neighbour.setName(capabilityApi.getName());

		Capabilities capabilitiesObject = new Capabilities();
		capabilitiesObject.setDataTypes(capabilityApi.getCapabilities());
		neighbour.setCapabilities(capabilitiesObject);

		return neighbour;
	}

	public ServiceProvider capabilityApiToServiceProvider(CapabilityApi capabilityApi){

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName(capabilityApi.getName());

		Capabilities serviceProviderCapabilities = new Capabilities();
		serviceProviderCapabilities.setDataTypes(capabilityApi.getCapabilities());
		serviceProvider.setCapabilities(serviceProviderCapabilities);

		return serviceProvider;
	}

	public CapabilityApi serviceProviderToCapabilityApi(ServiceProvider serviceProvider){

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(serviceProvider.getName());
		capabilityApi.setCapabilities(serviceProvider.getCapabilities().getDataTypes());

		return capabilityApi;
	}
}
