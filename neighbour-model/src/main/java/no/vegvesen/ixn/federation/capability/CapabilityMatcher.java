package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DataType;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	public static Set<String> calculateNeighbourSubscriptions(Set<Capability> capabilities, Set<DataType> subscriptions) {
		Set<String> subscriptionSelectors = subscriptions.stream().map(DataType::toSelector).collect(Collectors.toSet());
		return calculateNeighbourSubscriptionsFromSelectors(capabilities, subscriptionSelectors);
	}

	public static Set<String> calculateNeighbourSubscriptionsFromSelectors(Set<Capability> capabilities, Set<String> subscriptionSelectors) {
		Set<JMSSelectorFilter> subscriptionJmsFilters = subscriptionSelectors.stream().map(JMSSelectorFilterFactory::get).collect(Collectors.toSet());
		Set<JMSSelectorFilter> match = new HashSet<>();
		for (Capability capability : capabilities) {
			for (CapabilityFilter capabilityFlatFilter : capability.getCapabilityFiltersFlat()) {
				for (JMSSelectorFilter selectorFilter : subscriptionJmsFilters) {
					if (selectorFilter.matches(capabilityFlatFilter)) {
						match.add(selectorFilter);
					}
				}
			}
		}
		return match.stream().map(JMSSelectorFilter::getSelector).collect(Collectors.toSet());
	}

}
