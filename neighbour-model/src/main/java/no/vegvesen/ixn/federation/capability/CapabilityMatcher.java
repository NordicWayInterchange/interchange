package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import no.vegvesen.ixn.federation.model.DataType;
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

@Component
public class CapabilityMatcher {

	private static Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	private static class DataTypeFilter implements Filterable {

		private final HashMap<String, Object> headers = new HashMap<>();

		DataTypeFilter(DataType dataType) {
			headers.put("how", dataType.getHow());
			headers.put("where", dataType.getWhere());
		}

		@Override
		public Object getHeader(String messageHeaderName) {
			Object headerValue = this.headers.get(messageHeaderName);
			if (headerValue == null) {
				throw new HeaderNotFoundException(String.format("Message header [%s] not a known capability attribute", messageHeaderName));
			}
			return headerValue;
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

	public static JMSSelectorFilter validateSelector(String selector) {
		if (selector.contains("\"") || selector.contains("`")) {
			throw new InvalidSelectorException("String values in selectors must be quoted with single quoutes: " + selector);
		}
		JMSSelectorFilter filter = null;
		try {
			filter = new JMSSelectorFilter(selector);
		} catch (ParseException | TokenMgrError | SelectorParsingException e) {
			throw new InvalidSelectorException("Could not parse selector " + selector);
		}
		notAlwaysTrue(filter);
		return filter;
	}

	private static void notAlwaysTrue(JMSSelectorFilter filter) {
		DataTypeFilter neverTrue = new DataTypeFilter(new DataType("-1", "-1", "-1"));
		if (filter.matches(neverTrue)){
			throw new SelectorAlwaysTrueException("Cannot subscribe to a filter that is always true: " + filter.getSelector());
		}
	}
}
