package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Interchange;
import org.springframework.stereotype.Component;

@Component
public class CapabilityTransformer {




	public CapabilityApi interchangeToCapabilityApi(Interchange interchange){

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(interchange.getName());
		capabilityApi.setCapabilities(interchange.getCapabilities().getDataTypes());

		return capabilityApi;

	}

	public Interchange capabilityApiToInterchange(CapabilityApi capabilityApi){

		Interchange interchange = new Interchange();
		interchange.setName(capabilityApi.getName());

		Capabilities capabilitiesObject = new Capabilities();
		capabilitiesObject.setDataTypes(capabilityApi.getCapabilities());
		interchange.setCapabilities(capabilitiesObject);

		return interchange;

	}
}
