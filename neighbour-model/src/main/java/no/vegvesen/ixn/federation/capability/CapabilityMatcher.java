package no.vegvesen.ixn.federation.capability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.monotch.dxp.solution.nw3selectorpoc.lib.ParseException;
import com.monotch.dxp.solution.nw3selectorpoc.lib.Trilean;
import com.monotch.quadtree.QuadTreeHelper;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.PropertyExpression;
import org.apache.qpid.server.filter.PropertyExpressionFactory;
import org.apache.qpid.server.filter.SelectorParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.monotch.dxp.solution.nw3selectorpoc.lib.SelectorParser;
import com.monotch.dxp.solution.nw3selectorpoc.lib.filter.TrileanExpression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
@Component
public class CapabilityMatcher {
	private static final String REGEX_ALL_WHITESPACE = "\\s+";
	private static final String QUAD_TREE_MATCH_PATTERN_START = "quadtree like '%,";
	private static final String QUAD_TREE_MATCH_PATTERN_END = "%'";
	private static final String ID_MATCH_PATTERN_START = "id like '%,";
	private static final String ID_MATCH_PATTERN_END = ",%'";
	private static final Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);
	private static final ObjectMapper mapper = new ObjectMapper();

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
		//String evaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree());
		return matchCapability(capability, whiteSpaceTrimmedSelector);
/*		if (evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree())) {
			return matchCapability(capability, trimQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree()));
		} else {
			return false;
		}*/
		//String quadTreeEvaluatedSelector = evaluateQuadTreeMatch(whiteSpaceTrimmedSelector, capability.getQuadTree());
		//JMSSelectorFilter selectorFilter = JMSSelectorFilterFactory.get(quadTreeEvaluatedSelector);
		/*boolean match = false;
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
		}*/

		//return matchCapability(capability, selector);
	}

	public static boolean matchCapability(Capability capability, String selector) {
		CommonCapabilityJsonObject cap = TransformCapabilityToCapabilityJson.transformAllCommonProperties(capability);
		if (selector.isEmpty()) {
			return false;
		}
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String capJson = ow.writeValueAsString(cap);

			return qtmatch(selector, capJson);

			/*Map<String, Object> map = mapper.readValue(capJson, Map.class);
			SelectorParser<Map<String, Object>> selectorParser = new SelectorParser<>();
			selectorParser.setPropertyExpressionFactory(value -> new PropertyExpression<Map<String, Object>>() {
				@Override
				public Object evaluate(Map<String, Object> object) {
					return object.get(value);
				}
			});
			TrileanExpression<Map<String, Object>> matcher = selectorParser.parse(selector);
			return matcher.matches(map) != Trilean.FALSE;*/
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
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
		return matchStringArrayValues(selectorFilter, capability);
	}

	private static boolean matchSpatem(SpatemCapability capability, JMSSelectorFilter selectorFilter) {
		return matchStringArrayValues(selectorFilter, capability);
	}

	private static boolean matchMapem(MapemCapability capability, JMSSelectorFilter selectorFilter) {
		return matchStringArrayValues(selectorFilter, capability);
	}

	private static boolean matchSrem(SremCapability capability, JMSSelectorFilter selectorFilter) {
		return matchStringArrayValues(selectorFilter, capability);
	}

	private static boolean matchSsem(SsemCapability capability, JMSSelectorFilter selectorFilter) {
		return matchStringArrayValues(selectorFilter, capability);
	}

	private static boolean matchCam(CamCapability capability, JMSSelectorFilter selectorFilter) {
		Map<String, String> mandatoryValues = capability.getSingleValues();
		return matchEnumValues(selectorFilter, mandatoryValues, MessageProperty.STATION_TYPE.getName(), capability.getStationTypes());
	}

	//String array values are properties where a property may contain several values, thus needing to be
	//prefixed and postfixed by a ','
	private static boolean matchStringArrayValues(JMSSelectorFilter selectorFilter, Capability capability) {
		CapabilityFilter capabilityFilter = new CapabilityFilter(capability.getSingleValues());
		Set<String> messagepropertyValues = new HashSet<>();
		boolean matches = selectorFilter.matches(capabilityFilter);
		logger.debug("Evaluated match {} against selector [{}] without array values because array values are empty for property",
				matches, selectorFilter.getSelector());
		return matches;
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

	public static String trimQuadTreeMatch(String selector, Set<String> capabilityQuadTrees) {
		if (!selector.toLowerCase().contains(QUAD_TREE_MATCH_PATTERN_START)) {
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

			if (evaluatedSelector.toLowerCase().substring(matchStart-4, matchStart).contains("and")) {
				evaluatedSelector = evaluatedSelector.substring(0, matchStart - 4) + "" + evaluatedSelector.substring(matchEnd + QUAD_TREE_MATCH_PATTERN_END.length());
			} else if (evaluatedSelector.toLowerCase().substring(matchStart - 4, matchStart).contains("or")) {
				evaluatedSelector = evaluatedSelector.substring(0, matchStart - 3) + "" + evaluatedSelector.substring(matchEnd + QUAD_TREE_MATCH_PATTERN_END.length());
			}
		}
		return evaluatedSelector;
	}

	private static String replaceSpatemMapemIdMatch(String selector) {
		if (!selector.toLowerCase().contains(ID_MATCH_PATTERN_START)) {
			return selector;
		}

		String evaluatedSelector = selector;
		int matchStart;
		int matchEnd;
		while (true) {
			matchStart = evaluatedSelector.toLowerCase().indexOf(ID_MATCH_PATTERN_START);
			if (matchStart < 0) {
				break;
			}
			matchEnd = evaluatedSelector.indexOf(ID_MATCH_PATTERN_END, matchStart);

			int beforeMatch = evaluatedSelector.toLowerCase().indexOf(matchStart);
			if (evaluatedSelector.toLowerCase().substring(beforeMatch, matchStart).contains("and")) {
				evaluatedSelector = evaluatedSelector.substring(0, matchStart - 4) + "" + evaluatedSelector.substring(matchEnd + ID_MATCH_PATTERN_END.length());
			} else if (evaluatedSelector.toLowerCase().substring(beforeMatch, matchStart).contains("or")) {
				evaluatedSelector = evaluatedSelector.substring(0, matchStart - 3) + "" + evaluatedSelector.substring(matchEnd + ID_MATCH_PATTERN_END.length());
			}
		}
		return evaluatedSelector;
	}

	public static boolean match(String selector, Map<String, Object> capabilityMap) {
		if (selector.isEmpty()) {
			return true;
		}
		try {

			SelectorParser<Map<String, Object>> selectorParser = new SelectorParser<>();
			selectorParser.setPropertyExpressionFactory(new PropertyExpressionFactory<Map<String, Object>>() {
				@Override
				public PropertyExpression<Map<String, Object>> createPropertyExpression(String value) {
					return new PropertyExpression<Map<String, Object>>() {
						@Override
						public Object evaluate(Map<String, Object> object) {
							return object.get(value);
						}
					};
				}
			});
			TrileanExpression<Map<String, Object>> matcher = selectorParser.parse(selector);
			return matcher.matches(capabilityMap) != Trilean.FALSE;


		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean qtmatch(String selector, String capabilityJson) {
		if (selector.isEmpty()) {
			return true;
		}
		try {
			Map<String, Object> map = mapper.readValue(capabilityJson, Map.class);

			ArrayList<String> capTree = (ArrayList<String>)map.get("quadTree");
			if(capTree != null && selector.contains("quadTree")) {
				// we have a quadtree, we need to handle it a bit differently
				System.out.println("we have quadTree: "+capTree);

				//get selector tiles:
				ArrayList<String> selTree = new ArrayList<>();
				Pattern startqt = Pattern.compile("quadTree\\s*LIKE\\s*\'%,|quadTree\\s*not\\s*LIKE\\s*\'%,",Pattern.CASE_INSENSITIVE);
				Pattern midqt = Pattern.compile("[0123]+",Pattern.CASE_INSENSITIVE);
				Pattern endqt = Pattern.compile("%'",Pattern.CASE_INSENSITIVE);
				Matcher matchstartqt = startqt.matcher(selector);
				Matcher matchmidqt = midqt.matcher(selector);
				Matcher matchendqt = endqt.matcher(selector);
				int index = 0;
				while(matchstartqt.find(index)) {
					int startend = matchstartqt.end();
					matchendqt.find();
					index = matchendqt.end();
					int endstart = matchendqt.start();
					String qt = selector.substring(startend, endstart);
					if(matchmidqt.region(startend, endstart).matches()) {
						System.out.println("qt: "+qt);
						selTree.add(qt);
					}
				}
				if(selTree.size() < 1)
					throw new ParseException("The selector contains quadtrees, but it does not match quadtree selector pattern. Is there a typo in the selector? Selector: "+selector);

				//find the smallest tile:
				int smallestSize = 0;
				for(String t : selTree) {
					if(t.length() > smallestSize) {
						smallestSize = t.length();
					}
				}
				System.out.println("smallest tile is size: "+smallestSize);

				// create tiles at the same size as the smalles selector tile, and search for a match.
				// TODO: what if the cap tile is smaller than the smallest sel tile -> Seems to be handled for now, no broken tests.
				for(String capQt : capTree) {
					if( ! overlaps(capQt, selTree)) { // if there is no overlap between the cap qt and the sel qt, we dont need to search.
						if(testTile(selector, map, capQt)) return true; //test the cap tile directly (should take care of cases where there is only NOT LIKE, and nothing else in the selector)
						continue;
					}

					// create a smaler tile within capQt the same size as the smallest selector tile.
					// TODO: could probably be optimized by finding the smallest sel tile within each capQt. we want to keep the tiles as large as possible.
					String searchTile = padTile(capQt, smallestSize);
					QuadTreeHelper.direction dir = QuadTreeHelper.direction.R; //step direction
					String downFromStart = QuadTreeHelper.getNeighbour(QuadTreeHelper.direction.D, capQt);
					String candidate = null;
					while(searchTile != null) {

						//check match;
						if(testTile(selector, map, searchTile)) return true;

						// find the next tile:
						candidate = QuadTreeHelper.getNeighbour(dir, searchTile);
						if( ! candidate.startsWith(capQt)) { // gone too far

							if(candidate.startsWith(downFromStart)) { // we are bellow the start tile. time to quit.
								System.out.println("done");
								searchTile = null;
							}

							// move down one tile
							searchTile = QuadTreeHelper.getNeighbour(QuadTreeHelper.direction.D, searchTile);
							if(searchTile.startsWith(downFromStart)) { // we are bellow the start tile. time to quit.
								System.out.println("down");
								searchTile = null;
							}
							if(dir == QuadTreeHelper.direction.R) { // start moving left
								dir = QuadTreeHelper.direction.L;
							}
							else {// start moving right
								dir = QuadTreeHelper.direction.R;
							}
						}
						else
							searchTile = candidate;
					}
				}
			}
			System.out.println("no qt found");
			return match(selector,map);

		} catch (IllegalStateException | JsonProcessingException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean testTile(String selector, Map<String,Object> map, String searchTile) {
		String newQt = ","+searchTile+",";
		map.put("quadTree",newQt);
		//System.out.println(map);
		if(match(selector, map)) {
			System.out.println("found match for: "+searchTile);
			return true;
		}
		System.out.println("no match for: "+searchTile);
		return false;
	}

	private static String padTile(String startTile, int finalLength) {
		System.out.println("padding st:"+startTile);
		StringBuilder res = new StringBuilder(startTile);
		int padLength = finalLength-startTile.length();
		if(startTile.length() > finalLength)
			padLength=1;
		for(int i=0;i<padLength;i++){
			res.append('0');
		}
		return res.toString();
	}

	private static boolean overlaps(String tile1, ArrayList<String> tileset) {
		for(String tile2 : tileset) {
			if(overlaps(tile1, tile2)) return true;
		}
		return false;
	}

	private static boolean overlaps(String tile1, String tile2) {
		return (tile1.startsWith(tile2) || tile2.startsWith(tile1));
	}
}
