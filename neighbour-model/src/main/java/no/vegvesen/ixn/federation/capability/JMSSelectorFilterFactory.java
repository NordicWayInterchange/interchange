package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.SelectorParsingException;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.filter.selector.TokenMgrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class JMSSelectorFilterFactory {
	private static Logger logger = LoggerFactory.getLogger(JMSSelectorFilterFactory.class);

	public static JMSSelectorFilter get(String selector) {
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

	public static boolean isValidSelector(String selector) {
		try {
			JMSSelectorFilterFactory.get(selector);
			return true;
		} catch (InvalidSelectorException | SelectorAlwaysTrueException e) {
			logger.error("Invalid selector {}", selector, e);
			return false;
		}
	}

	private static void notAlwaysTrue(JMSSelectorFilter filter) {
		HashMap<String, String> neverTrueValues = new HashMap<>();
		neverTrueValues.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "-1");
		DataTypeFilter neverTrue = new DataTypeFilter(new DataType(neverTrueValues));
		if (filter.matches(neverTrue)){
			throw new SelectorAlwaysTrueException("Cannot subscribe to a filter that is always true: " + filter.getSelector());
		}
	}
}
