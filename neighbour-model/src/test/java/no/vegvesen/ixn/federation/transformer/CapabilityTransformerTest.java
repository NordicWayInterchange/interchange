package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
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
		final String how = "datex2;1.0";
		final String where = "NO";
		DataTypeApi capabilities = new DataTypeApi(how, where);
		capabilityApi.setCapabilities(Collections.singleton(capabilities));

		Neighbour interchange = capabilityTransformer.capabilityApiToNeighbour(capabilityApi);

		assertThat(interchange.getCapabilities().getDataTypes()).hasSize(1);
		assertThat(interchange.getCapabilities().getDataTypes()).containsExactly(new DataType(how,where));
	}
}
