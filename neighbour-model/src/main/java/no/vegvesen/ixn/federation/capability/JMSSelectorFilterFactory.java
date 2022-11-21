package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.properties.CapabilityProperty;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.SelectorParsingException;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.filter.selector.TokenMgrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

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
		} catch (InvalidSelectorException | SelectorAlwaysTrueException | HeaderNotFoundException e) {
			logger.error("Invalid selector {}", selector, e);
			return false;
		}
	}

	private static void notAlwaysTrue(JMSSelectorFilter filter) {
		CapabilityFilter neverTrue = new CapabilityFilter(Collections.singletonMap(CapabilityProperty.ORIGINATING_COUNTRY.getName(), "-1"));
		if (filter.matches(neverTrue)){
			throw new SelectorAlwaysTrueException("Cannot subscribe to a filter that is always true: " + filter.getSelector());
		}
	}
}
