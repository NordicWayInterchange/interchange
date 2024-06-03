package no.vegvesen.ixn.federation.capability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.matcher.SelectorCapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	private static final Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	private static SelectorCapabilityMatcher matcher = new SelectorCapabilityMatcher();

	public static Set<LocalSubscription> calculateNeighbourSubscriptionsFromSelectors(Set<Capability> capabilities, Set<LocalSubscription> subscriptionSelectors, String ixnName) {
		Set<LocalSubscription> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			for (LocalSubscription selector : subscriptionSelectors) {
				if (!selector.getSelector().isEmpty()) {
					if (matchConsumerCommonNameToRedirectPolicy(selector.getConsumerCommonName(), capability.getMetadata().getRedirectPolicy(), ixnName)) {
						boolean match = matchCapabilityToSelector(capability, selector.getSelector());
						if (match) {
							logger.debug("Selector [{}] matches capability {}", selector, capability);
							matches.add(selector);
						}
					}
				}
			}
		}
		return matches;
	}

	private static boolean matchConsumerCommonNameToRedirectPolicy(String consumerCommonName, RedirectStatus redirectStatus, String ixnName) {
		if (consumerCommonName.equals(ixnName)) {
			return !redirectStatus.equals(RedirectStatus.MANDATORY);
		} else {
			return !redirectStatus.equals(RedirectStatus.NOT_AVAILABLE);
		}
	}

	public static Set<Capability> matchCapabilitiesToSelector(Set<Capability> capabilities, String selector) {
		Set<Capability> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			boolean match = matchCapabilityToSelector(capability, selector);
			if (match) {
				logger.debug("Selector [{}] matches capability {}", selector, capability);
				matches.add(capability);
			}
		}
		return matches;
	}

	public static boolean matchCapabilityToSelector(Capability capability, String selector) {
		ObjectMapper mapper = new ObjectMapper();
		String capabilityJson = null;
		try {
			capabilityJson = mapper.writeValueAsString(capability.getApplication());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return matcher.match(selector, capabilityJson);
	}

	public static boolean matchCapabilityShardToSelector(Capability capability, Integer shardId, String selector) {
		ObjectMapper mapper = new ObjectMapper();
		String capabilityJson = null;
		try {
			capabilityJson = mapper.writeValueAsString(capability.getApplication()).replace("}", ",\"shardId\":" + shardId + "}");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		System.out.println(capabilityJson);
		return matcher.match(selector, capabilityJson);
	}
}
