package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.SelectorParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	private static final String REGEX_ALL_WHITESPACE = "\\s+";
	private static final String QUAD_TREE_MATCH_PATTERN_START = "quadtree like '%,";
	private static final String QUAD_TREE_MATCH_PATTERN_END = "%'";
	private static final Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	public static Set<LocalSubscription> calculateNeighbourSubscriptionsFromSelectors(Set<CapabilitySplit> capabilities, Set<LocalSubscription> subscriptionSelectors, String ixnName) {
		Set<LocalSubscription> matches = new HashSet<>();
		for (CapabilitySplit capability : capabilities) {
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

	public static Set<CapabilitySplit> matchCapabilitiesToSelector(Set<CapabilitySplit> capabilities, String selector) {
		Set<CapabilitySplit> matches = new HashSet<>();
		for (CapabilitySplit capability : capabilities) {
			boolean match = matchCapabilityToSelector(capability, selector);
			if (match) {
				logger.debug("Selector [{}] matches capability {}", selector, capability);
				matches.add(capability);
			}
		}
		return matches;
	}

	public static boolean matchLocalDeliveryToServiceProviderCapabilities(Set<CapabilitySplit> capabilities, LocalDelivery delivery) {
		String selector = delivery.getSelector();
		boolean finalMatch = false;
		for (CapabilitySplit capability : capabilities) {
			finalMatch = matchCapabilityToSelector(capability, selector);
		}
		return finalMatch;
	}

	public static boolean matchCapabilityToSelector(CapabilitySplit capability, String selector) {
		logger.debug("Evaluating selector [{}] against capability {}", selector, capability);
		String whiteSpaceTrimmedSelector = selector.replaceAll(REGEX_ALL_WHITESPACE, " ");
		String quadTreeEvaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getApplication().getQuadTree());
		JMSSelectorFilter selectorFilter = JMSSelectorFilterFactory.get(quadTreeEvaluatedSelector);
		boolean match = false;
		if (capability.getApplication() instanceof DatexApplication) {
			match = matchDatex((DatexApplication) capability.getApplication(), selectorFilter);
		} else if (capability.getApplication() instanceof DenmApplication) {
			match = matchDenm((DenmApplication) capability.getApplication(), selectorFilter);
		} else if (capability.getApplication() instanceof IvimApplication) {
			match = matchIvi((IvimApplication) capability.getApplication(), selectorFilter);
		} else if (capability.getApplication() instanceof SpatemApplication) {
			match = matchSpatem((SpatemApplication) capability.getApplication(), selectorFilter);
		} else if (capability.getApplication() instanceof MapemApplication) {
			match = matchMapem((MapemApplication) capability.getApplication(), selectorFilter);
		} else if (capability.getApplication() instanceof SremApplication) {
			match = matchSrem((SremApplication) capability.getApplication(), selectorFilter);
		} else if (capability.getApplication() instanceof SsemApplication) {
			match = matchSsem((SsemApplication) capability.getApplication(), selectorFilter);
		} else if (capability.getApplication() instanceof CamApplication) {
			match = matchCam((CamApplication) capability.getApplication(), selectorFilter);
		} else {
			logger.warn("Unknown Capability type {} ", capability.getClass().getName());
		}
		if (match) {
			logger.debug("Selector [{}] matches capability {}", selector, capability);
			return true;
		}
		return false;
	}

	private static boolean matchDatex(DatexApplication capability, JMSSelectorFilter selectorFilter) {
		Map<String, Object> mandatoryValues = capability.getSingleValuesBase();
		return matchSingleStringValue(selectorFilter, mandatoryValues, MessageProperty.PUBLICATION_TYPE.getName(), capability.getPublicationType());
	}

	private static boolean matchDenm(DenmApplication application, JMSSelectorFilter selectorFilter) {
		return matchEnumValues(selectorFilter, application.getSingleValuesBase(), MessageProperty.CAUSE_CODE.getName(), application.getCauseCode());
	}

	private static boolean matchIvi(IvimApplication application, JMSSelectorFilter selectorFilter) {
		Map<String, Object> mandatoryValues = application.getSingleValuesBase();
		return matchApplicationValues(selectorFilter, mandatoryValues);
	}

	private static boolean matchSpatem(SpatemApplication application, JMSSelectorFilter selectorFilter) {
		Map<String, Object> mandatoryValues = application.getSingleValuesBase();
		return matchApplicationValues(selectorFilter, mandatoryValues);
	}

	private static boolean matchMapem(MapemApplication application, JMSSelectorFilter selectorFilter) {
		Map<String, Object> mandatoryValues = application.getSingleValuesBase();
		return matchApplicationValues(selectorFilter, mandatoryValues);
	}

	private static boolean matchSrem(SremApplication application, JMSSelectorFilter selectorFilter) {
		Map<String, Object> mandatoryValues = application.getSingleValuesBase();
		return matchApplicationValues(selectorFilter, mandatoryValues);
	}

	private static boolean matchSsem(SsemApplication application, JMSSelectorFilter selectorFilter) {
		Map<String, Object> mandatoryValues = application.getSingleValuesBase();
		return matchApplicationValues(selectorFilter, mandatoryValues);
	}

	private static boolean matchCam(CamApplication application, JMSSelectorFilter selectorFilter) {
		Map<String, Object> mandatoryValues = application.getSingleValuesBase();
		return matchApplicationValues(selectorFilter, mandatoryValues);
	}

	private static boolean matchApplicationValues(JMSSelectorFilter selectorFilter, Map<String, Object> mandatoryValues) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(mandatoryValues);
		boolean matches = selectorFilter.matches(capabilityFilter);
		logger.debug("Evaluated match {} against selector [{}]",
				matches, selectorFilter.getSelector());
		return matches;
	}

	//String array values are properties where a property may contain several values, thus needing to be
	//prefixed and postfixed by a ','
	private static boolean matchStringArrayValues(JMSSelectorFilter selectorFilter, CapabilitySplit capability, MessageProperty messageProperty, Set<String> propertyValues) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(capability.getApplication().getSingleValuesBase());
		Set<String> messagepropertyValues = new HashSet<>();
		for (String value : propertyValues) {
			messagepropertyValues.add("," + value + ",");
		}
		if (propertyValues.isEmpty()){
			boolean matches = selectorFilter.matches(capabilityFilter);
			logger.debug("Evaluated match {} against selector [{}] without array values because array values are empty for property {}",
					matches, selectorFilter.getSelector(), messageProperty.getName());
			return matches;
		}
		for (String propertyValue : messagepropertyValues) {
			capabilityFilter.putValue(messageProperty.getName(), propertyValue);
			if (selectorFilter.matches(capabilityFilter)) {
				logger.debug("array value matches selector [{}] on property [{}] with value [{}].",
						selectorFilter.getSelector(), messageProperty.getName(), propertyValue);
				return true;
			}
		}
		return false;
	}

	// Enum values are properties where a property may have only one value in the message headers, but multiple possible
	// values may be specified in the Capability, thus making an enum-type.
	private static boolean matchEnumValues(JMSSelectorFilter selectorFilter, Map<String, Object> mandatoryValues, String propertyName, Set<Integer> propertyValues) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(mandatoryValues);
		if (propertyValues.isEmpty()){
			boolean matches = selectorFilter.matches(capabilityFilter);
			logger.debug("Evaluated match {} against selector [{}] without array values because array values are empty for property {}",
					matches, selectorFilter.getSelector(), propertyName);
			return matches;
		}
		for (Integer propertyValue : propertyValues) {
			capabilityFilter.putValue(propertyName, propertyValue);
			if (selectorFilter.matches(capabilityFilter)) {
				logger.debug("array value matches selector [{}] on property [{}] with value [{}].",
						selectorFilter.getSelector(), propertyName, propertyValue);
				return true;
			}
		}
		return false;
	}

	private static boolean matchSingleStringValue(JMSSelectorFilter selectorFilter, Map<String, Object> mandatoryValues, String propertyName, String propertyValue) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(mandatoryValues);
		if (propertyValue == null){
			boolean matches = selectorFilter.matches(capabilityFilter);
			logger.debug("Evaluated match {} against selector [{}] without array values because array values are empty for property {}",
					matches, selectorFilter.getSelector(), propertyName);
			return matches;
		}
		capabilityFilter.putValue(propertyName, propertyValue);
		if (selectorFilter.matches(capabilityFilter)) {
			logger.debug("string value matches selector [{}] on property [{}] with value [{}].",
					selectorFilter.getSelector(), propertyName, propertyValue);
			return true;
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
			if (matchEnd < 0) {
				throw new SelectorParsingException("Could not find end of quad tree match started at index " + matchStart);
			}
			String selectorQuadTreeTile = evaluatedSelector.substring(matchStart + QUAD_TREE_MATCH_PATTERN_START.length(), matchEnd).trim();

			logger.debug("Checking capability quadTreeTile against selector quadTree tile [{}]", selectorQuadTreeTile);
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
}
