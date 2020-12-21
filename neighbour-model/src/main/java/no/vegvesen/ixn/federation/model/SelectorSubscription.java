package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.capability.DataTypeFilter;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;

import java.util.Set;

public class SelectorSubscription {

	private String selector;
	private Set<String> quadTreeTiles;

	public SelectorSubscription(String selector, Set<String> quadTreeTiles) {
		this.selector = selector;
		this.quadTreeTiles = quadTreeTiles;
	}

	public boolean matches(DataType capabilities) {
		Set<String> capabilitiesQuadTreeTiles = capabilities.getPropertyValueAsSet(MessageProperty.QUAD_TREE);
		JMSSelectorFilter jmsSelectorFilter = JMSSelectorFilterFactory.get(selector);
		DataTypeFilter capabilitiesFilter = new DataTypeFilter(capabilities);
		return jmsSelectorFilter.matches(capabilitiesFilter) && matchesQuadTreeTiles(capabilities.getPropertyValueAsSet(MessageProperty.QUAD_TREE));
	}

	private boolean matchesQuadTreeTiles(Set<String> capabilitiesQuadTreeTiles) {
		if (capabilitiesQuadTreeTiles.isEmpty() || quadTreeTiles.isEmpty()) {
			return true;
		}
		for (String v1Value : capabilitiesQuadTreeTiles) {
			for (String v2Value : quadTreeTiles) {
				if (v1Value.startsWith(v2Value) || v2Value.startsWith(v1Value)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getSelector() {
		return selector;
	}

	public Set<String> getQuadTreeTiles() {
		return quadTreeTiles;
	}
}
