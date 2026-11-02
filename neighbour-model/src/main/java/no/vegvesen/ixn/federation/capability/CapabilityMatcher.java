package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.matcher.SelectorCapabilityMatcher;
import no.vegvesen.ixn.federation.matcher.Trilean;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.RedirectStatus;
import no.vegvesen.ixn.federation.model.capability.Application;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	private static final Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	private static SelectorCapabilityMatcher matcher = new SelectorCapabilityMatcher();

	private static SelectorCapabilityMatcher strictMatcher = new SelectorCapabilityMatcher(Trilean.FALSE);


	public static Set<LocalSubscription> calculateNeighbourSubscriptionsFromSelectors(Set<NeighbourCapability> capabilities, Set<LocalSubscription> subscriptionSelectors, String ixnName) {
		Set<LocalSubscription> matches = new HashSet<>();
		for (NeighbourCapability capability : capabilities) {
			for (LocalSubscription subscription : subscriptionSelectors) {
				if (!subscription.getSelector().isEmpty()) {
					if (matchConsumerCommonNameToRedirectPolicy(subscription.getConsumerCommonName(), capability.getMetadata().getRedirectPolicy(), ixnName)) {
						boolean match = matchApplicationToSelector(capability.getApplication(), subscription.getSelector(), capability.getMetadata().getShardCount());
						if (match) {
							logger.debug("Selector [{}] matches capability {}", subscription.getSelector(), capability);
							matches.add(subscription);
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

	public static Set<NeighbourCapability> matchNeighbourCapabilitiesToSelector(Set<NeighbourCapability> capabilities, String selector) {
		Set<NeighbourCapability> matches = new HashSet<>();
		for (NeighbourCapability capability : capabilities) {
			boolean match = matchApplicationToSelector(capability.getApplication(), selector, capability.getMetadata().getShardCount());
			if (match) {
				logger.debug("Selector [{}] matches capability {}", selector, capability);
				matches.add(capability);
			}
		}
		return matches;
	}

	public static Set<NeighbourCapability> matchNeighbourCapabilitiesToSelectorStrict(Set<NeighbourCapability> capabilities, String selector) {
		Set<NeighbourCapability> matches = new HashSet<>();
		for (NeighbourCapability capability : capabilities) {
			boolean match = matchApplicationToSelector(capability.getApplication(), selector, capability.getMetadata().getShardCount(), strictMatcher);
			if (match) {
				logger.debug("Selector [{}] strictly matches capability {}", selector, capability);
				matches.add(capability);
			}
		}
		return matches;
	}

	public static Set<Capability> matchCapabilitiesToSelector(Set<Capability> capabilities, String selector) {
		Set<Capability> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			boolean match = matchApplicationToSelector(capability.getApplication(), selector, capability.getMetadata().getShardCount());
			if (match) {
				logger.debug("Selector [{}] matches capability {}", selector, capability);
				matches.add(capability);
			}
		}
		return matches;
	}

	public static Set<Capability> matchCapabilitiesToSelectorStrict(Set<Capability> capabilities, String selector) {
		Set<Capability> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			boolean match = matchApplicationToSelector(capability.getApplication(), selector, capability.getMetadata().getShardCount(), strictMatcher);
			if (match) {
				logger.debug("Selector [{}] strictly matches capability {}", selector, capability);
				matches.add(capability);
			}
		}
		return matches;
	}

	public static boolean matchApplicationToSelector(Application application, String selector, Integer shardCount) {
		return matchApplicationToSelector(application, selector, shardCount, matcher);
	}

	private static boolean matchApplicationToSelector(Application application, String selector, Integer shardCount, SelectorCapabilityMatcher matcherToUse) {
		ObjectMapper mapper = JsonMapper.builder().build();
		String capabilityJson = null;
		if (shardCount > 1) {
			for (int i = 0; i < shardCount; i++) {
				int shardId = i + 1;
				if (matchCapabilityApplicationWithShardToSelector(application, shardId, selector, matcherToUse)) {
					return true;
				}
			}
		} else {
			if (!selectorIsSharded(selector)) {
				try {
					capabilityJson = mapper.writeValueAsString(application);
				} catch (JacksonException e) {
					throw new RuntimeException(e);
				}
				return matcherToUse.match(selector, capabilityJson);
			}
		}
		return false;
	}

	public static boolean matchCapabilityApplicationWithShardToSelector(Application application, Integer shardId, String selector) {
		return matchCapabilityApplicationWithShardToSelector(application, shardId, selector, matcher);
	}

	private static boolean matchCapabilityApplicationWithShardToSelector(Application application, Integer shardId, String selector, SelectorCapabilityMatcher matcherToUse) {
		ObjectMapper mapper = JsonMapper.builder().build();
		String capabilityJson = null;
		try {
			capabilityJson = mapper.writeValueAsString(application).replace("}", ",\"shardId\":" + shardId + "}");
		} catch (JacksonException e) {
			throw new RuntimeException(e);
		}
		return matcherToUse.match(selector, capabilityJson);
	}
	public static boolean selectorIsSharded(String selector) {
		return selector.contains("shardId");
	}
}
