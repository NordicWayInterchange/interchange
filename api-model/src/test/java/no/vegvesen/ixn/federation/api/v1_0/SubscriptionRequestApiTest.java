package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.HashSet;

public class SubscriptionRequestApiTest {

	@Test
	public void toJsonString() throws JsonProcessingException {
		HashSet<SubscriptionApi> subscriptions = new HashSet<>();
		subscriptions.add(new SubscriptionApi("(quadTree like '%,ABCD% OR quadTree like '%,BCDE%') AND messageType = 'DATEX2'", null, SubscriptionStatus.REQUESTED));
		SubscriptionRequestApi spOneSubscriptionRequest = new SubscriptionRequestApi("sp-one.bouvetinterchange.no", subscriptions);
		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(objectMapper.writeValueAsString(spOneSubscriptionRequest));
	}
}