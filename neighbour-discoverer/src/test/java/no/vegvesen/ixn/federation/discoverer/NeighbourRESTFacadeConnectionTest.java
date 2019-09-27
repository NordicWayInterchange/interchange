package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityTransformer;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionTransformer;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public class NeighbourRESTFacadeConnectionTest {


    @Test(expected = CapabilityPostException.class)
    public void test() {
        DataType dataType = new DataType();
		dataType.setHow("datex2.0;1");
		dataType.setWhere("NO");
		CapabilityApi capabilityApi = new CapabilityApi("remote server", Collections.singleton(dataType));
		Neighbour nonexistent = new Neighbour();
		nonexistent.setName("localhost");
		nonexistent.setControlChannelPort("9876");
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();
		CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
		SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
		SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

		NeighbourRESTFacade neighbourRESTFacade = new NeighbourRESTFacade(
			restTemplate,
			capabilityTransformer,
			subscriptionTransformer,
			subscriptionRequestTransformer,
			mapper);

		Neighbour response = neighbourRESTFacade.postCapabilities(nonexistent, nonexistent);
    }
}
