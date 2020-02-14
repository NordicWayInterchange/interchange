package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataTypeSelectorMatcher {

	private static Logger logger = LoggerFactory.getLogger(DataTypeSelectorMatcher.class);

	public static boolean matches(DataType capability, String selector) {
	    JMSSelectorFilter filter = JMSSelectorFilterFactory.get(selector);
		DataTypeFilter capabilityFilter = new DataTypeFilter(capability);
		return filter.matches(capabilityFilter);
	}

	public static Set<String> calculateCommonInterestSelectors(Set<DataType> dataTypes, Set<String> selectors) {
		Set<String> calculatedSelectors = new HashSet<>();
		for (DataType neighbourDataType : dataTypes) {
			for (String selector : selectors) {
				try {
					// Trows InvalidSelectorException if selector is invalid or SelectorAlwaysTrueException if selector is always true
					logger.info("Matching local subscription {}",selector);
					if (matches(neighbourDataType, selector)) {
					    calculatedSelectors.add(selector);

					}
                } catch (InvalidSelectorException | SelectorAlwaysTrueException | HeaderNotFoundException e) {
					logger.error("Error matching neighbour data type with local subscription. Skipping selector {}",selector, e);
                }
			}
		}
		return calculatedSelectors;
	}

	private static class QuadTreeFilter {
		private final Set<String> quadTreeTiles;

		QuadTreeFilter(Set<String> quadTreeTiles) {
			this.quadTreeTiles = quadTreeTiles;
		}

		boolean matches(DataType capabilityDataType) {
			Collection<String> capabilitiesQuadTree = capabilityDataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE);
			if (capabilitiesQuadTree.isEmpty() || quadTreeTiles.isEmpty()) {
				return true;
			}
			for (String filterTile : quadTreeTiles) {
				for (String capabilityTile : capabilitiesQuadTree) {
					if (filterTile.startsWith(capabilityTile) || capabilityTile.startsWith(filterTile)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
