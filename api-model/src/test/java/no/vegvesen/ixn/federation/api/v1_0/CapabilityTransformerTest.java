package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityTransformerTest {


	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();

	@Test
	public void capabilitiyApiIsConvertedToInterchangeAndBack(){
		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName("Test 2");
		DataTypeApi capabilities = new DataTypeApi("datex2;1.0", "NO");
		capabilityApi.setCapabilities(Collections.singleton(capabilities));

		Neighbour interchange = capabilityTransformer.capabilityApiToNeighbour(capabilityApi);

		assertThat(interchange.getCapabilities().getDataTypes()).hasSize(1).contains(capabilities);
	}
}
