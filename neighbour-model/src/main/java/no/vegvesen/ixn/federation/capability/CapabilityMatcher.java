package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	private static final String REGEX_ALL_WHITESPACE = "\\s+";
	private static final String QUAD_TREE_MATCH_PATTERN_START = "quadtree like '%,";
	private static final String QUAD_TREE_MATCH_PATTERN_END = "%'";
	private static final Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	public static Set<LocalSubscription> calculateNeighbourSubscriptionsFromSelectors(Set<Capability> capabilities, Set<LocalSubscription> subscriptionSelectors) {
		Set<LocalSubscription> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			for (LocalSubscription selector : subscriptionSelectors) {
				logger.debug("Evaluating selector [{}] against capability {}", selector, capability);
				String whiteSpaceTrimmedSelector = selector.getSelector().replaceAll(REGEX_ALL_WHITESPACE, " ");
				String quadTreeEvaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree());
				JMSSelectorFilter selectorFilter = JMSSelectorFilterFactory.get(quadTreeEvaluatedSelector);
				boolean match = false;
				if (capability instanceof DatexCapability) {
					match = matchDatex((DatexCapability) capability, selectorFilter);
				} else if (capability instanceof DenmCapability) {
					match = matchDenm((DenmCapability) capability, selectorFilter);
				} else if (capability instanceof IvimCapability) {
					match = matchIvi((IvimCapability) capability, selectorFilter);
				} else {
					logger.warn("Unknown Capability type {} ", capability.getClass().getName());
				}
				if (match) {
					logger.debug("Selector [{}] matches capability {}", selector, capability);
					matches.add(selector);
				}
			}
		}
		return matches;
	}

	private static boolean matchDatex(DatexCapability capability, JMSSelectorFilter selectorFilter) {
		return matchArrayValues(selectorFilter, capability, MessageProperty.PUBLICATION_TYPE, capability.getPublicationTypes());
	}

	private static boolean matchDenm(DenmCapability capability, JMSSelectorFilter selectorFilter) {
		return matchArrayValues(selectorFilter, capability, MessageProperty.CAUSE_CODE, capability.getCauseCodes());
	}

	private static boolean matchIvi(IvimCapability capability, JMSSelectorFilter selectorFilter) {
		return matchArrayValues(selectorFilter, capability, MessageProperty.IVI_TYPE, capability.getIviTypes());
	}

	private static boolean matchArrayValues(JMSSelectorFilter selectorFilter, Capability capability, MessageProperty messageProperty, Set<String> propertyValues) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(capability.getSingleValues());
		if (propertyValues.isEmpty()){
			boolean matches = selectorFilter.matches(capabilityFilter);
			logger.debug("Evaluated match {} against selector [{}] without array values because array values are empty for property {}",
					matches, selectorFilter.getSelector(), messageProperty.getName());
			return matches;
		}
		for (String propertyValue : propertyValues) {
			capabilityFilter.putValue(messageProperty.getName(), propertyValue);
			if (selectorFilter.matches(capabilityFilter)) {
				logger.debug("array value matches selector [{}] on property [{}] with value [{}].",
						selectorFilter.getSelector(), messageProperty.getName(), propertyValue);
				return true;
			}
		}
		return false;
	}

	private static String evaluateQuadTreeMatch(String selector, Set<String> capabilityQuadTrees) {
		if (!selector.toLowerCase().contains(QUAD_TREE_MATCH_PATTERN_START)) {
			logger.debug("Selector with no quadTree tiles specified needs no custom quadTree matching: [{}]", selector);
			return selector;
		} else if (capabilityQuadTrees.isEmpty()) {
			logger.debug("Capabilities with no quadTree tiles specified needs no custom quadTree matching");
			return selector;
		}
		String evaluatedSelector = selector;
		int matchEnd;
		int matchStart;
		while (true) {
			matchStart = evaluatedSelector.toLowerCase().indexOf(QUAD_TREE_MATCH_PATTERN_START);
			if (matchStart < 0) {
				break;
			}
			matchEnd = evaluatedSelector.indexOf(QUAD_TREE_MATCH_PATTERN_END, matchStart);
			String selectorQuadTreeTile = evaluatedSelector.substring(matchStart + QUAD_TREE_MATCH_PATTERN_START.length(), matchEnd).trim();

			logger.info("Checking capability quadTreeTile against selector quadTree tile [{}]", selectorQuadTreeTile);
			boolean gotMatch = false;
			for (String capabilityQuadTreeTile : capabilityQuadTrees) {
				if (selectorQuadTreeTile.startsWith(capabilityQuadTreeTile) || capabilityQuadTreeTile.startsWith(selectorQuadTreeTile)) {
					gotMatch = true;
					logger.debug("Capability quadTree tile [{}] matching selector quadTree tile [{}]", capabilityQuadTreeTile, selectorQuadTreeTile);
					break;
				}
			}
			if (gotMatch)
				evaluatedSelector = evaluatedSelector.substring(0, matchStart) + "true" + evaluatedSelector.substring(matchEnd + QUAD_TREE_MATCH_PATTERN_END.length());
			else
				evaluatedSelector = evaluatedSelector.substring(0, matchStart) + "false" + evaluatedSelector.substring(matchEnd + QUAD_TREE_MATCH_PATTERN_END.length());
		}
		logger.debug("Selector with quadTree tiles [{}] evaluated to : [{}] with capability quadTree tiles {}", selector, evaluatedSelector, capabilityQuadTrees);
		return evaluatedSelector;
	}

	public static Set<Capability> matchCapabilityToSubscriptionSelector(Set<Capability> capabilities, Subscription subscriptionSelector) {
		Set<Capability> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			String whiteSpaceTrimmedSelector = subscriptionSelector.getSelector().replaceAll(REGEX_ALL_WHITESPACE, " ");
			String quadTreeEvaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree());
			JMSSelectorFilter selectorFilter = JMSSelectorFilterFactory.get(quadTreeEvaluatedSelector);
			boolean match = false;
			if (capability instanceof DatexCapability) {
				match = matchDatex((DatexCapability) capability, selectorFilter);
			} else if (capability instanceof DenmCapability) {
				match = matchDenm((DenmCapability) capability, selectorFilter);
			} else if (capability instanceof IvimCapability) {
				match = matchIvi((IvimCapability) capability, selectorFilter);
			} else {
				logger.warn("Unknown Capability type {} ", capability.getClass().getName());
			}
			if (match) {
				logger.debug("Selector [{}] matches capability {}", subscriptionSelector, capability);
				matches.add(capability);
			}
		}
		return matches;
	}

	public static boolean matchLocalDeliveryToServiceProviderCapabilities(Set<Capability> capabilities, LocalDelivery delivery) {
		String selector = delivery.getSelector();
		boolean finalMatch = false;
		for (Capability capability : capabilities) {
			logger.debug("Evaluating selector [{}] against capability {}", selector, capability);
			String whiteSpaceTrimmedSelector = selector.replaceAll(REGEX_ALL_WHITESPACE, " ");
			String quadTreeEvaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree());
			JMSSelectorFilter selectorFilter = JMSSelectorFilterFactory.get(quadTreeEvaluatedSelector);
			boolean match = false;
			if (capability instanceof DatexCapability) {
				match = matchDatex((DatexCapability) capability, selectorFilter);
			} else if (capability instanceof DenmCapability) {
				match = matchDenm((DenmCapability) capability, selectorFilter);
				System.out.println(match);
			} else {
				logger.warn("Unknown Capability type {} ", capability.getClass().getName());
			}
			if (match) {
				logger.debug("Selector [{}] matches capability {}", selector, capability);
				finalMatch = true;
			}
		}
		return finalMatch;
	}
}
