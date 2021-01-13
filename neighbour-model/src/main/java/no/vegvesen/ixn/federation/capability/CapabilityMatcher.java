package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	private static final String REGEX_ALL_WHITESPACE = "\\s+";
	private static final String QUAD_TREE_MATCH_PATTERN_START = "quadtree like '%,";
	private static final String QUAD_TREE_MATCH_PATTERN_END = "%'";
	private static final Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	public static Set<String> calculateNeighbourSubscriptions(Set<Capability> capabilities, Set<DataType> subscriptions) {
		Set<String> subscriptionSelectors = subscriptions.stream().map(DataType::toSelector).collect(Collectors.toSet());
		return calculateNeighbourSubscriptionsFromSelectors(capabilities, subscriptionSelectors);
	}

	public static Set<String> calculateNeighbourSubscriptionsFromSelectors(Set<Capability> capabilities, Set<String> subscriptionSelectors) {
		Set<String> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			for (String selector : subscriptionSelectors) {
				String whiteSpaceTrimmedSelector = selector.replaceAll(REGEX_ALL_WHITESPACE, " ");
				String quadTreeEvaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree());
				JMSSelectorFilter selectorFilter = JMSSelectorFilterFactory.get(quadTreeEvaluatedSelector);
				boolean match = false;
				if (capability instanceof DatexCapability) {
					match = matchDatex((DatexCapability) capability, selectorFilter);
				} else if (capability instanceof DenmCapability) {
					match = matchDenm((DenmCapability) capability, selectorFilter);
				} else if (capability instanceof IviCapability) {
					match = matchIvi((IviCapability) capability, selectorFilter);
				} else {
					logger.warn("Unknown Capability type {} ", capability.getClass().getName());
				}
				if (match) {
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

	private static boolean matchIvi(IviCapability  capability, JMSSelectorFilter selectorFilter) {
		return matchArrayValues(selectorFilter, capability, MessageProperty.IVI_TYPE, capability.getIviTypes());
	}

	private static boolean matchArrayValues(JMSSelectorFilter selectorFilter, Capability capability, MessageProperty messageProperty, Set<String> propertyValues) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(capability.getSingleValues());
		if (propertyValues.isEmpty()){
			return selectorFilter.matches(capabilityFilter);
		}
		for (String propertyValue : propertyValues) {
			capabilityFilter.putValue(messageProperty.getName(), propertyValue);
			if (selectorFilter.matches(capabilityFilter)) {
				logger.debug("quadTree-evaluated selector filter [{}] matches capability [{}].", selectorFilter.toString(), capabilityFilter.toString());
				return true;
			}
		}
		return false;
	}

	private static String evaluateQuadTreeMatch(String selector, Set<String> capabilityQuadTrees) {
		if (!selector.toLowerCase().contains(QUAD_TREE_MATCH_PATTERN_START)) {
			logger.debug("Selector with no quadTree tiles specified needs no custom quadTree matching: {}", selector);
			return selector;
		} else if (capabilityQuadTrees.isEmpty()) {
			logger.debug("Capabilities with no quadTree tiles specified needs no custom quadTree matching");
			return selector;
		}
		String evaluatedSelector = selector;
		int matchEnd = 0;
		int matchStart = 0;
		while (matchStart >= 0 && matchEnd >= 0) {
			matchStart = evaluatedSelector.toLowerCase().indexOf(QUAD_TREE_MATCH_PATTERN_START);
			if (matchStart < 0) {
				break;
			}
			matchEnd = evaluatedSelector.indexOf(QUAD_TREE_MATCH_PATTERN_END, matchStart); // could it end differetly?
			String selectorQuadTreeTile = evaluatedSelector.substring(matchStart + QUAD_TREE_MATCH_PATTERN_START.length(), matchEnd).trim();

			logger.info("checking capability quadTreeTile against  " + selectorQuadTreeTile);
			boolean gotMatch = false;
			for (String capabilityQuadTreeTile : capabilityQuadTrees) {
				if (selectorQuadTreeTile.startsWith(capabilityQuadTreeTile) || capabilityQuadTreeTile.startsWith(selectorQuadTreeTile)) {
					gotMatch = true;
					break;
				}
			}
			if (gotMatch)
				evaluatedSelector = evaluatedSelector.substring(0, matchStart) + "true" + evaluatedSelector.substring(matchEnd + 2);
			else
				evaluatedSelector = evaluatedSelector.substring(0, matchStart) + "false" + evaluatedSelector.substring(matchEnd + 2);
		}
		return evaluatedSelector;
	}


}
