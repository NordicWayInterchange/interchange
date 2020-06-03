package no.vegvesen.ixn.federation.capability;

/*-
 * #%L
 * neighbour-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
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
		} catch (InvalidSelectorException | SelectorAlwaysTrueException | HeaderNotFoundException e) {
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
