package no.vegvesen.ixn.federation.discoverer.capability;

import no.vegvesen.ixn.federation.model.Model.DataType;
import org.apache.qpid.server.filter.Filterable;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.message.AMQMessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class CapabilityMatcher {

	private static Logger logger = LoggerFactory.getLogger(CapabilityMatcher.class);

	private static class DataTypeFilter implements Filterable {

		private final HashMap<String, Object> headers = new HashMap<>();

		public DataTypeFilter(DataType dataType) {
			headers.put("how", dataType.getHow());
			headers.put("version", dataType.getVersion());
			headers.put("what", dataType.getWhat());
		}

		@Override
		public Object getHeader(String messageHeaderName) {
			Object headerValue = this.headers.get(messageHeaderName);
			if (headerValue == null) {
				throw new IllegalArgumentException(String.format("Message header [%s] not a known capability attribute", messageHeaderName));
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

	@SuppressWarnings("WeakerAccess")
	public static boolean matches(DataType capability, String selector) throws ParseException {
		DataTypeFilter capabilityFilter = new DataTypeFilter(capability);
		JMSSelectorFilter filter = new JMSSelectorFilter(selector);
		return filter.matches(capabilityFilter);
	}
}
