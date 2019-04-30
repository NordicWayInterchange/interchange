package no.vegvesen.ixn.federation;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class NeighbourRestControllerTest {


	private MockMvc mockMvc;

	@Mock
	InterchangeRepository interchangeRepository;

	@Mock
	ServiceProviderRepository serviceProviderRepository;

	@InjectMocks
	private NeighbourRestController neighbourRestController;

	private String subscriptionRequestPath = "/requestSubscription";
	private String capabilityExchangePath = "/updateCapabilities";

	@Before
	public void setUp(){
		mockMvc = MockMvcBuilders.standaloneSetup(neighbourRestController).build();
	}


	private ObjectMapper objectMapper = new ObjectMapper();

	private boolean checkThatAllSubscriptionHaveStatusRequested(Set<Subscription> subscriptions){

		for(Subscription s : subscriptions){
			if(s.getSubscriptionStatus() != Subscription.SubscriptionStatus.REQUESTED){
				return false;
			}
		}
		return true;
	}

	private String capabilitiesPostString() throws Exception{
		Interchange bouvet = new Interchange();
		bouvet.setName("bouvet");

		Capabilities bouvetCapabilities = new Capabilities();
		bouvetCapabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);

		DataType bouvetDataType = new DataType("datex2;1.0", "NO", "Obstruction");
		bouvetCapabilities.setDataTypes(Collections.singleton(bouvetDataType));

		return objectMapper.writeValueAsString(bouvet);
	}

	@Test
	public void listOfSubscriptionHaveStatusSetToRequested(){

		Subscription first = new Subscription();
		first.setSubscriptionStatus(Subscription.SubscriptionStatus.CREATED);
		Subscription second = new Subscription();
		second.setSubscriptionStatus(Subscription.SubscriptionStatus.REJECTED);

		Set<Subscription> subscriptionsStatusRequested = neighbourRestController.setStatusRequestedForAllSubscriptions(Stream.of(first, second).collect(Collectors.toSet()));

		Assert.assertTrue(checkThatAllSubscriptionHaveStatusRequested(subscriptionsStatusRequested));

	}

	@Test
	public void postingCapabilitiesReturnsStatusCreated() throws Exception{

		String bouvetJson = capabilitiesPostString();

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(bouvetJson))
				.andExpect(status().isCreated());
	}

	@Test
	public void postingSubscriptionRequestReturnsStatusAccepted() throws Exception{
		Interchange bouvet = new Interchange();
		bouvet.setName("bouvet");

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		Subscription subscription = new Subscription("where1 LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));

		bouvet.setSubscriptionRequest(subscriptionRequest);

		String bouvetJson = objectMapper.writeValueAsString(bouvet);

		Interchange updatedInterchange = new Interchange();
		updatedInterchange.setName("bouvet");

		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		updatedInterchange.setCapabilities(capabilities);

		Subscription firstSubscription = new Subscription("where1 LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/bouvet/subscription/1");
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		updatedInterchange.setSubscriptionRequest(returnedSubscriptionRequest);


		doReturn(updatedInterchange).when(interchangeRepository).save(any(Interchange.class));


		mockMvc.perform(post(subscriptionRequestPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(bouvetJson))
				.andExpect(status().isAccepted());

	}
}
