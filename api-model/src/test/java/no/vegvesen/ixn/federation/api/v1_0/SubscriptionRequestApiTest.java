package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Sets;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SubscriptionRequestApiTest {

	@Test
	public void toJsonString() throws JsonProcessingException {
		HashSet<SubscriptionApi> subscriptions = new HashSet<>();
		Set<String> quads = Sets.newLinkedHashSet("ABCD", "BCDE");
		subscriptions.add(new SubscriptionApi("messageType = 'DATEX2'", null, quads, SubscriptionStatus.REQUESTED));
		SubscriptionRequestApi spOneSubscriptionRequest = new SubscriptionRequestApi("sp-one.bouvetinterchange.no", subscriptions);
		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(objectMapper.writeValueAsString(spOneSubscriptionRequest));
	}
}