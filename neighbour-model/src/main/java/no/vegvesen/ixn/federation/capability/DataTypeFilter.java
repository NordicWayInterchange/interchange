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

import no.vegvesen.ixn.federation.exceptions.HeaderNotFilterable;
import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.server.filter.Filterable;
import org.apache.qpid.server.message.AMQMessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

class DataTypeFilter implements Filterable {

	private static Logger logger = LoggerFactory.getLogger(DataTypeFilter.class);

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
