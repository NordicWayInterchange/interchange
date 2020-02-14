package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.SelectorParsingException;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.filter.selector.TokenMgrError;

import java.util.HashMap;

public class SelectorValidator {
	public static JMSSelectorFilter validate(String selector) {

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
}
