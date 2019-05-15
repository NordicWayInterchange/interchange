package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertTrue;

public class CapabilityTransformerTest {


	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();

	@Test
	public void interchangeIsConvertedToCapabilityApiAndBack(){

		Interchange interchange = new Interchange();
		interchange.setName("Test 1");
		DataType dataType = new DataType("datex2;1.0", "NO", "Conditions");
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(Collections.singleton(dataType));
		interchange.setCapabilities(capabilities);

		CapabilityApi capabilityApi = capabilityTransformer.interchangeToCapabilityApi(interchange);
		Interchange transformed = capabilityTransformer.capabilityApiToInterchange(capabilityApi);

		for(DataType d : transformed.getCapabilities().getDataTypes()){
			assertTrue(dataType.getHow().equals(d.getHow()) && dataType.getWhere().equals(d.getWhere()));
		}

		assertTrue(interchange.getName().equals(transformed.getName()) );
	}

	@Test
	public void capabilitiyApiIsConvertedToInterchangeAndBack(){
		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName("Test 2");
		DataType capabilities = new DataType("datex2;1.0", "NO", "Conditions");
		capabilityApi.setCapabilities(Collections.singleton(capabilities));

		Interchange interchange = capabilityTransformer.capabilityApiToInterchange(capabilityApi);
		CapabilityApi transformed = capabilityTransformer.interchangeToCapabilityApi(interchange);

		for(DataType d : transformed.getCapabilities()){
			assertTrue(capabilities.getHow().equals(d.getHow()) && capabilities.getWhere().equals(d.getWhere()));
		}

		assertTrue(capabilityApi.getName().equals(transformed.getName()));

	}
}
