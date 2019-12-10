package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.exceptions.HeaderNotFoundException;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.model.BaseDataType;
import no.vegvesen.ixn.federation.model.DataTypeHeader;
import org.apache.qpid.server.filter.Filterable;
import org.apache.qpid.server.filter.JMSSelectorFilter;
import org.apache.qpid.server.filter.selector.ParseException;
import org.apache.qpid.server.message.AMQMessageHeader;

import java.util.HashMap;

public class BaseDataTypeSelectorMatcher {


    private static class DataTypeFilter implements Filterable {

        private final HashMap<String,Object> headers = new HashMap<>();

        DataTypeFilter(BaseDataType dataType) {
            for (DataTypeHeader header : dataType.getHeaders()) {
                headers.put(header.getHeaderName(),header.getHeaderValue());
            }

        }

        @Override
        public Object getHeader(String name) {
            Object headerValue = headers.get(name);
            if (headerValue == null) {
                throw new HeaderNotFoundException(String.format("Message header [%s] is not a known capability attribute",name));
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
    }

    public static boolean matches(BaseDataType dataType, String selector) {
        try {
            JMSSelectorFilter  filter = new JMSSelectorFilter(selector);
            //notAlwaysTrue(filter);
            DataTypeFilter capabilityFilter = new DataTypeFilter(dataType);
            return filter.matches(capabilityFilter);
        } catch (ParseException e) {
            throw new InvalidSelectorException(String.format("Could not parse selector \"%s\"",selector));
        }
    }
/*
    private static void notAlwaysTrue(JMSSelectorFilter filter) {
        DataTypeFilter neverTrue = new DataTypeFilter(new BaseDataType(Arrays.asList(new DataTypeHeader("messageType","-1"))));
        if (filter.matches(neverTrue)) {
            throw new SelectorAlwaysTrueException("Cannot subscribe to a filter that is always true: " + filter.getSelector());
        }

    }

    */
}
