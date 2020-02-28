package no.vegvesen.ixn.util;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;

import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("RedundantThrows")
public class MessageTestDouble implements Message {

    private final Map<String, String> properties;
    private long expiration = 0L;

    public MessageTestDouble(Map<String,String> properties) {

        this.properties = properties;
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        return "id";
    }

    @Override
    public void setJMSMessageID(String id) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setJMSTimestamp(long timestamp) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public String getJMSType() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSType(String type) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public long getJMSExpiration() throws JMSException {
        return expiration;
    }

    @Override
    public void setJMSExpiration(long expiration) throws JMSException {
        this.expiration = expiration;
    }

    @Override
    public int getJMSPriority() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSPriority(int priority) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void clearProperties() throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public boolean propertyExists(String name) throws JMSException {
        return properties.get(name) != null;
    }

    @Override
    public boolean getBooleanProperty(String name) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public byte getByteProperty(String name) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public short getShortProperty(String name) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public int getIntProperty(String name) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public long getLongProperty(String name) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public float getFloatProperty(String name) throws JMSException {
        return Float.valueOf(properties.get(name));
    }

    @Override
    public double getDoubleProperty(String name) throws JMSException {
        return Double.valueOf(properties.get(name));
    }

    @Override
    public String getStringProperty(String name) throws JMSException {
        return properties.get(name);
    }

    @Override
    public Object getObjectProperty(String name) throws JMSException {
        return properties.get(name);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public Enumeration getPropertyNames() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setByteProperty(String name, byte value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setShortProperty(String name, short value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setIntProperty(String name, int value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setLongProperty(String name, long value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setFloatProperty(String name, float value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setDoubleProperty(String name, double value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setStringProperty(String name, String value) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void setObjectProperty(String name, Object value) throws JMSException {

        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void acknowledge() throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public void clearBody() throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public long getJMSDeliveryTime() throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {
        throw new IllegalStateException("Method not implemented");

    }

    @Override
    public <T> T getBody(Class<T> c) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public boolean isBodyAssignableTo(Class c) throws JMSException {
        throw new IllegalStateException("Method not implemented");
    }

   //All the properties are defined as String, just for ease.
    public static Message createMessage(String publisher,
        String originatingCountry,
        String protocolVersion,
        String messageType,
        String latitude,
        String longitude,
        KeyValue... additionalProperties) {

        Map<String, String> properties = new HashMap<>();
        properties.put(MessageProperty.PUBLISHER_NAME.getName(),publisher);
        properties.put(MessageProperty.ORIGINATING_COUNTRY.getName(),originatingCountry);
        properties.put(MessageProperty.PROTOCOL_VERSION.getName(),protocolVersion);
        properties.put(MessageProperty.MESSAGE_TYPE.getName(),messageType);
        properties.put(MessageProperty.LATITUDE.getName(),latitude);
        properties.put(MessageProperty.LONGITUDE.getName(),longitude);
        for (KeyValue kv : additionalProperties) {
            properties.put(kv.getKey(),kv.getValue());
        }
        return new MessageTestDouble(properties);
    }

    public static Message createDatexMessage(String publisher,
        String originatingCountry,
        String protocolVersion,
        String latitude,
        String longitude,
        KeyValue... additionalProperties) {
        return createMessage(publisher,
                originatingCountry,
                protocolVersion,
				Datex2DataTypeApi.DATEX_2,
                latitude,
                longitude,
                additionalProperties);
    }

}
