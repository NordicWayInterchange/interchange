package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.Filterable;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.SelectorParsingException;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.filter.selector.TokenMgrError;
import org.apache.qpid.server.message.AMQMessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataTypeSelectorMatcher {

	private static Logger logger = LoggerFactory.getLogger(DataTypeSelectorMatcher.class);

	private static class DataTypeFilter implements Filterable {

		private final HashMap<String, Object> headers = new HashMap<>();

		DataTypeFilter(DataType dataType) {
			Set<MessageProperty> allPropertyNames = MessageProperty.filterableProperties;
			for (MessageProperty property: allPropertyNames) {
				headers.put(property.getName(), dataType.getPropertyValue(property));
			}
		}

		@Override
		public Object getHeader(String messageHeaderName) {
			if (!this.headers.containsKey(messageHeaderName)) {
				if (messageHeaderName.equals(MessageProperty.QUAD_TREE.getName())){
					throw new HeaderNotFilterable(String.format("Message header [%s] must be specified in separate attribute outside selector filter", messageHeaderName));
				}
				if (MessageProperty.nonFilterablePropertyNames.contains(messageHeaderName)) {
					throw new HeaderNotFilterable(String.format("Message header [%s] not possible to use in selector filter", messageHeaderName));
				}
				throw new HeaderNotFoundException(String.format("Message header [%s] not a known capability attribute", messageHeaderName));
			}
			Object value = this.headers.get(messageHeaderName);
			logger.debug("Getting header [{}] with value [{}] of type {}", messageHeaderName, value, value == null ? null : value.getClass().getSimpleName());
			return value;
		}

		@Override
		public AMQMessageHeader getMessageHeader() {
			throw new IllegalArgumentException("AMQMessageHeader not implemented for capabilities matching");
		}

		@Override
		public boolean isPersistent() {
			throw new IllegalArgumentException("persistent not implemented for capabilities matching");
		}

		@Override
		public boolean isRedelivered() {
			throw new IllegalArgumentException("redelivered not implemented for capabilities matching");
		}

		@Override
		public Object getConnectionReference() {
			throw new IllegalArgumentException("connectionReference not implemented for capabilities matching");
		}

		@Override
		public long getMessageNumber() {
			throw new IllegalArgumentException("messageNumber not implemented for capabilities matching");
		}

		@Override
		public long getArrivalTime() {
			throw new IllegalArgumentException("arrivalTime not implemented for capabilities matching");
		}


		@Override
		public String getReplyTo() {
			throw new IllegalArgumentException("replyTo not implemented for capabilities matching");
		}

		@Override
		public String getType() {
			throw new IllegalArgumentException("type not implemented for capabilities matching");
		}

		@Override
		public byte getPriority() {
			throw new IllegalArgumentException("priority not implemented for capabilities matching");
		}

		@Override
		public String getMessageId() {
			throw new IllegalArgumentException("messageId not implemented for capabilities matching");
		}

		@Override
		public long getTimestamp() {
			throw new IllegalArgumentException("timestamp not implemented for capabilities matching");
		}

		@Override
		public String getCorrelationId() {
			throw new IllegalArgumentException("correlationId not implemented for capabilities matching");
		}

		@Override
		public long getExpiration() {
			throw new IllegalArgumentException("expiration not implemented for capabilities matching");
		}
	}

	public static boolean matches(DataType capability, String selector) {
	    JMSSelectorFilter filter = validateSelector(selector);
		DataTypeFilter capabilityFilter = new DataTypeFilter(capability);
		return filter.matches(capabilityFilter);
	}

	static boolean matches(DataType capability, Set<String> quadTreeFilter, String jmsSelectorFilter) {
	    JMSSelectorFilter selectorFilter = validateSelector(jmsSelectorFilter);
		DataTypeFilter capabilityFilter = new DataTypeFilter(capability);
		QuadTreeFilter quadTree = new QuadTreeFilter(quadTreeFilter);
		return selectorFilter.matches(capabilityFilter) && quadTree.matches(capability);

	}

	public static JMSSelectorFilter validateSelector(String selector) {
		if (selector.contains("\"") || selector.contains("`")) {
			throw new InvalidSelectorException("String values in selectors must be quoted with single quoutes: " + selector);
		}
		JMSSelectorFilter filter;
		try {
			filter = new JMSSelectorFilter(selector);
		} catch (ParseException | TokenMgrError | SelectorParsingException e) {
			throw new InvalidSelectorException(String.format("Could not parse selector \"%s\"",selector));
		}
		notAlwaysTrue(filter);
		return filter;
	}

	private static void notAlwaysTrue(JMSSelectorFilter filter) {
		HashMap<String, String> neverTrueValues = new HashMap<>();
		neverTrueValues.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "-1");
		DataTypeFilter neverTrue = new DataTypeFilter(new DataType(neverTrueValues));
		if (filter.matches(neverTrue)){
			throw new SelectorAlwaysTrueException("Cannot subscribe to a filter that is always true: " + filter.getSelector());
		}
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

		boolean matches(DataType dataType) {
			String [] quadTree = dataType.getPropertyValueAsArray(MessageProperty.QUAD_TREE);
			for (String filterTile : quadTreeTiles) {
				for (String capabilityTile : quadTree) {
					if (filterTile.startsWith(capabilityTile) || capabilityTile.startsWith(filterTile)) {
						return true;
					}
				}
			}
			return false;
		}
	}
}
