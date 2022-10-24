package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.SelectorParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	private static final String REGEX_ALL_WHITESPACE = "\\s+";
	private static final String QUAD_TREE_MATCH_PATTERN_START = "quadtree like '%,";
	private static final String QUAD_TREE_MATCH_PATTERN_END = "%'";
	private static final Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	public static Set<LocalSubscription> calculateNeighbourSubscriptionsFromSelectors(Set<Capability> capabilities, Set<LocalSubscription> subscriptionSelectors, String ixnName) {
		Set<LocalSubscription> matches = new HashSet<>();
		for (Capability capability : capabilities) {
			for (LocalSubscription selector : subscriptionSelectors) {
				if (!selector.getSelector().isEmpty()) {
					if (matchConsumerCommonNameToRedirectPolicy(selector.getConsumerCommonName(), capability.getRedirect(), ixnName)) {
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

	public static boolean matchLocalDeliveryToServiceProviderCapabilities(Set<Capability> capabilities, LocalDelivery delivery) {
		String selector = delivery.getSelector();
		boolean finalMatch = false;
		for (Capability capability : capabilities) {
			finalMatch = matchCapabilityToSelector(capability, selector);
		}
		return finalMatch;
	}

	public static boolean matchCapabilityToSelector(Capability capability, String selector) {
		logger.debug("Evaluating selector [{}] against capability {}", selector, capability);
		String whiteSpaceTrimmedSelector = selector.replaceAll(REGEX_ALL_WHITESPACE, " ");
		String quadTreeEvaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree());
		JMSSelectorFilter selectorFilter = JMSSelectorFilterFactory.get(quadTreeEvaluatedSelector);
		boolean match = false;
		if (capability instanceof DatexCapability) {
			match = matchDatex((DatexCapability) capability, selectorFilter);
		} else if (capability instanceof DenmCapability) {
			match = matchDenm((DenmCapability) capability, selectorFilter);
		} else if (capability instanceof IvimCapability) {
			match = matchIvi((IvimCapability) capability, selectorFilter);
		} else if (capability instanceof SpatemCapability) {
			match = matchSpatem((SpatemCapability) capability, selectorFilter);
		} else if (capability instanceof MapemCapability) {
			match = matchMapem((MapemCapability) capability, selectorFilter);
		} else if (capability instanceof SremCapability) {
			match = matchSrem((SremCapability) capability, selectorFilter);
		} else if (capability instanceof SsemCapability) {
			match = matchSsem((SsemCapability) capability, selectorFilter);
		} else if (capability instanceof CamCapability) {
			match = matchCam((CamCapability) capability, selectorFilter);
		} else {
			logger.warn("Unknown Capability type {} ", capability.getClass().getName());
		}
		if (match) {
			logger.debug("Selector [{}] matches capability {}", selector, capability);
			return true;
		}
		return false;
	}

	private static boolean matchDatex(DatexCapability capability, JMSSelectorFilter selectorFilter) {
		Map<String, String> mandatoryValues = capability.getSingleValues();
		return matchEnumValues(selectorFilter, mandatoryValues, MessageProperty.PUBLICATION_TYPE.getName(), capability.getPublicationTypes());
	}

	//TODO the DENM cause code is NOT an array value! It's a string value with multiple possible values
	//AND it's mandatory
	private static boolean matchDenm(DenmCapability capability, JMSSelectorFilter selectorFilter) {
		return matchEnumValues(selectorFilter, capability.getSingleValues(), MessageProperty.CAUSE_CODE.getName(), capability.getCauseCodes());
	}

	private static boolean matchIvi(IvimCapability capability, JMSSelectorFilter selectorFilter) {
		boolean c = matchStringArrayValues(selectorFilter, capability, MessageProperty.IVI_TYPE, capability.getIviTypes());
		return c;
	}

	private static boolean matchSpatem(SpatemCapability capability, JMSSelectorFilter selectorFilter) {
		boolean m = matchStringArrayValues(selectorFilter, capability, MessageProperty.IDS, capability.getIds());
		return m;
	}

	private static boolean matchMapem(MapemCapability capability, JMSSelectorFilter selectorFilter) {
		boolean m = matchStringArrayValues(selectorFilter, capability, MessageProperty.IDS, capability.getIds());
		return m;
	}

	private static boolean matchSrem(SremCapability capability, JMSSelectorFilter selectorFilter) {
		boolean m = matchStringArrayValues(selectorFilter, capability, MessageProperty.IDS, capability.getIds());
		return m;
	}

	private static boolean matchSsem(SsemCapability capability, JMSSelectorFilter selectorFilter) {
		boolean m = matchStringArrayValues(selectorFilter, capability, MessageProperty.IDS, capability.getIds());
		return m;
	}

	private static boolean matchCam(CamCapability capability, JMSSelectorFilter selectorFilter) {
		Map<String, String> mandatoryValues = capability.getSingleValues();
		return matchEnumValues(selectorFilter, mandatoryValues, MessageProperty.STATION_TYPE.getName(), capability.getStationTypes());
	}

	//String array values are properties where a property may contain several values, thus needing to be
	//prefixed and postfixed by a ','
	private static boolean matchStringArrayValues(JMSSelectorFilter selectorFilter, Capability capability, MessageProperty messageProperty, Set<String> propertyValues) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(capability.getSingleValues());
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
	private static boolean matchEnumValues(JMSSelectorFilter selectorFilter, Map<String, String> mandatoryValues, String propertyName, Set<String> propertyValues) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(mandatoryValues);
		if (propertyValues.isEmpty()){
			boolean matches = selectorFilter.matches(capabilityFilter);
			logger.debug("Evaluated match {} against selector [{}] without array values because array values are empty for property {}",
					matches, selectorFilter.getSelector(), propertyName);
			return matches;
		}
		for (String propertyValue : propertyValues) {
			capabilityFilter.putValue(propertyName, propertyValue);
			if (selectorFilter.matches(capabilityFilter)) {
				logger.debug("array value matches selector [{}] on property [{}] with value [{}].",
						selectorFilter.getSelector(), propertyName, propertyValue);
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
			if (matchEnd < 0) {
				throw new SelectorParsingException("Could not find end of quad tree match started at index " + matchStart);
			}
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
}
