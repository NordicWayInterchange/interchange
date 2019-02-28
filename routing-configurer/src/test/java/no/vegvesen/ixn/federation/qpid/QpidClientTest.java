package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.model.Subscription;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class QpidClientTest {

	@Test
	public void createBindingWithTwoSubscriptionsWillBeJoinedWithLogicalOperatorOR() {
		QpidClient c = new QpidClient("aaa", "aaa", mock(RestTemplate.class));
		HashSet<Subscription> selectors = new HashSet<>();
		selectors.add(new Subscription("abc", "a", null, null));
		selectors.add(new Subscription("abc", "b", null, null));
		assertThat(c.createBinding(selectors)).contains("(a)").contains(" OR ").contains("(b)");
	}

	@Test
	public void createBindingWithOneSubscriptionWillBeGuardedWithParentheses() {
		QpidClient c = new QpidClient("aaa", "aaa", mock(RestTemplate.class));
		HashSet<Subscription> selectors = new HashSet<>();
		selectors.add(new Subscription("abc", "a", null, null));
		assertThat(c.createBinding(selectors)).isEqualTo("(a)");
	}
}