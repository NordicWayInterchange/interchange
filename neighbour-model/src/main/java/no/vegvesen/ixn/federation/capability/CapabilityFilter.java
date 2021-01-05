package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.Filterable;
import org.apache.qpid.server.message.AMQMessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CapabilityFilter implements Filterable {

	private static Logger logger = LoggerFactory.getLogger(CapabilityFilter.class);

	private final HashMap<String, Object> headers = new HashMap<>();

	public CapabilityFilter(Map<String, Object> capabilityFlat) {
		headers.putAll(capabilityFlat);
	}

	@Override
	public Object getHeader(String messageHeaderName) {
		if (!this.headers.containsKey(messageHeaderName)) {
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

	@Override
	public String toString() {
		return "CapabilityFilter{" +
				"headers=" + headers +
				'}';
	}
}
