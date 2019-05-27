package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
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

		assertThat(transformed.getCapabilities().getDataTypes()).hasSize(1).contains(dataType);
	}

	@Test
	public void capabilitiyApiIsConvertedToInterchangeAndBack(){
		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName("Test 2");
		DataType capabilities = new DataType("datex2;1.0", "NO", "Conditions");
		capabilityApi.setCapabilities(Collections.singleton(capabilities));

		Interchange interchange = capabilityTransformer.capabilityApiToInterchange(capabilityApi);
		CapabilityApi transformed = capabilityTransformer.interchangeToCapabilityApi(interchange);

		assertThat(transformed.getCapabilities()).hasSize(1).contains(capabilities);
	}
}
